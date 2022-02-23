/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.x7.reyc.internal;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import io.xream.x7.base.api.BackendService;
import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.BusyException;
import io.xream.x7.base.exception.RemoteBizException;
import io.xream.x7.base.exception.ReyException;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.util.StringUtil;
import io.xream.x7.reyc.api.ReyTemplate;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * wrapped resilience4j: Retry,CircuitBreaker</br>
 * Retry>CircuitBreaker>RateLimiter>Bulkhead  </br>
 * but connection problem will retry immediately
 *
 * @author Sim
 */
public class R4JTemplate implements ReyTemplate {

    private static Logger logger = LoggerFactory.getLogger(ReyTemplate.class);

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;

    private ReyProperties reyProperties;

    public R4JTemplate(ReyProperties reyProperties) {
        this.reyProperties = reyProperties;
    }

    public void wrap(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public String support(String config, boolean isRetry, BackendService<String> backendService) {

        return support(config, config, isRetry, backendService);
    }

    @Override
    public String support(String handlerName, String config, boolean isRetry, BackendService<String> backendService) {

        if (StringUtil.isNullOrEmpty(config)) {
            config = "";
        }

        final String configName = config.equals("") ? "default" : config;

        CircuitBreakerConfig circuitBreakerConfig = circuitBreakerRegistry.getConfiguration(configName).orElse(circuitBreakerRegistry.getDefaultConfig());

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(handlerName, circuitBreakerConfig);
        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, backendService::handle);

        if (isRetry) {
            RetryConfig retryConfig = retryRegistry.getConfiguration(configName).orElse(retryRegistry.getDefaultConfig());
            Retry retry = retryRegistry.retry(handlerName, retryConfig);
            if (retry != null) {

                retry.getEventPublisher()
                        .onRetry(event -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug(event.getEventType().toString() + "_" + event.getNumberOfRetryAttempts() + ": backend("
                                        + handlerName + ")");
                            }
                        });

                decoratedSupplier = Retry
                        .decorateSupplier(retry, decoratedSupplier);
            }
        }

        String logStr = "Backend(" + handlerName + ")";

        String result = Try.ofSupplier(decoratedSupplier)
                .recover(e ->
                        hanleException(e, logStr, backendService)
                ).get();


        return result;
    }

    private String hanleException(Throwable e, String tag, BackendService<String> backendService) {

        if (logger.isErrorEnabled()) {
            logger.error(tag + ": " + e.getMessage());
        }

        if (e instanceof CallNotPermittedException) {
            Object obj = backendService.fallback();
            throw new BusyException(obj == null ? null : obj.toString());
        }

        if (e instanceof ResourceAccessException){
            backendService.fallback();
            ResourceAccessException rae = (ResourceAccessException)e;
            Throwable t = rae.getRootCause();
            System.out.println("___clzz:"+rae.getRootCause().getClass().getName());
            String str = rae.getLocalizedMessage();
            String[] arr = str.split(";");
            final String message = arr[0];
            if (t instanceof ConnectException) {
                throw ReyException.create(ReyHttpStatus.TO_CLIENT, -1 ,message,null,null);
            }else if (t instanceof SocketTimeoutException) {
                throw ReyException.create(ReyHttpStatus.TO_CLIENT, -2 ,message,null,null);
            }

        }else if (e instanceof HttpClientErrorException){
            backendService.fallback();
            HttpClientErrorException ee = (HttpClientErrorException)e;
            String str = ee.getLocalizedMessage();
            System.out.println("_____000: " + str);
            str = str.split(": ")[1].trim();
            str = str.replace("[","");
            str = str.replace("]","");
            Map<String,Object> map = JsonX.toMap(str);
            String message = MapUtils.getString(map, "path");
            HttpClientErrorException hee = (HttpClientErrorException)e;
            throw ReyException.create(ReyHttpStatus.TO_CLIENT, hee.getStatusCode().value() ,message,null,null);
        }else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException hse = (HttpServerErrorException)e;
            String str = hse.getLocalizedMessage();
            System.out.println("_____111: " + str);
            str = str.split(": ")[1].trim();
            str = str.replace("[","");
            str = str.replace("]","");
            if (!str.endsWith("\"}")) {
                str += "\"}";
            }
            Map<String,Object> map = JsonX.toMap(str);
            throw ReyException.create(ReyHttpStatus.INTERNAL_SERVER_ERROR, hse.getStatusCode().value() ,
                    MapUtils.getString(map,"message"),
                    MapUtils.getString(map,"stack"),
                    MapUtils.getString(map,"traceId")
            );
        }

        throw new RemoteBizException(e);
    }


}
