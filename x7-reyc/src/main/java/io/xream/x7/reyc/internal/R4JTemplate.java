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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import io.xream.x7.base.api.BackendService;
import io.xream.x7.base.exception.ReyBizException;
import io.xream.x7.base.util.StringUtil;
import io.xream.x7.base.web.ResponseString;
import io.xream.x7.reyc.api.ReyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ClientExceptionHandler clientExceptionHandler;

    public R4JTemplate(ClientExceptionHandler clientExceptionHandler) {
        this.clientExceptionHandler = clientExceptionHandler;
    }

    public void wrap(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public String support(String config, boolean isRetry, BackendService<ResponseString> backendService) {

        return support(config, config, isRetry, backendService);
    }

    @Override
    public String support(String handlerName, String config, boolean isRetry, BackendService<ResponseString> backendService) {

        if (StringUtil.isNullOrEmpty(config)) {
            config = "";
        }

        final String configName = config.equals("") ? "default" : config;

        CircuitBreakerConfig circuitBreakerConfig = circuitBreakerRegistry.getConfiguration(configName).orElse(circuitBreakerRegistry.getDefaultConfig());

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(handlerName, circuitBreakerConfig);
        Supplier<ResponseString> decoratedSupplier = CircuitBreaker
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

        final String tag = "Backend(" + handlerName + ")";

        ResponseString response = Try.ofSupplier(decoratedSupplier)
                .recover(e -> {
                        logger.error(tag + ": " + e.getMessage());
                        return handleException(e); }
                ).get();

        if (! isNotFallback(response.getStatus())) {
            Object obj = backendService.fallback();
            if (obj != null) {
                throw new ReyBizException(tag + " FALLBACK",obj);
            }
        }

        this.clientExceptionHandler.resolver().convertNot200ToException(
                response.getStatus(),
                response.getBody()
        );

        return response.getBody();
    }

    private ResponseString handleException(Throwable e) {

        return this.clientExceptionHandler.resolver().handleException(e);
    }

    private boolean isNotFallback(int status){
        return this.clientExceptionHandler.resolver()
                .fallbackHandler().isNotFallback(status);
    }


}
