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
package io.xream.rey.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import io.xream.internal.util.StringUtil;
import io.xream.rey.api.BackendService;
import io.xream.rey.api.ReyTemplate;
import io.xream.rey.exception.ReyInternalException;
import io.xream.rey.exception.ReyRuntimeException;
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
public class R4JTemplate<T> implements ReyTemplate<T> {

    private static Logger logger = LoggerFactory.getLogger(ReyTemplate.class);

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;


    public void wrap(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public T support(String configName, boolean isRetry, BackendService<T> backendService) throws ReyInternalException{

        return support(configName, configName, isRetry, backendService);
    }

    @Override
    public T support(String serviceName, String backendName, boolean isRetry, BackendService<T> backendService) throws ReyInternalException{

        if (StringUtil.isNullOrEmpty(backendName)) {
            backendName = "";
        }

        final String configName = backendName.equals("") ? "default" : backendName;

        CircuitBreakerConfig circuitBreakerConfig = circuitBreakerRegistry.getConfiguration(configName).orElse(circuitBreakerRegistry.getDefaultConfig());

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName, circuitBreakerConfig);
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, backendService::handle);

        if (isRetry) {
            RetryConfig retryConfig = retryRegistry.getConfiguration(configName).orElse(retryRegistry.getDefaultConfig());
            Retry retry = retryRegistry.retry(serviceName, retryConfig);
            if (retry != null) {

                retry.getEventPublisher()
                        .onRetry(event -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug(event.getEventType().toString() + "_" + event.getNumberOfRetryAttempts() + ": backend("
                                        + serviceName + ")");
                            }
                        });

                decoratedSupplier = Retry
                        .decorateSupplier(retry, decoratedSupplier);
            }
        }

        final String tag = "Backend(" + serviceName + ")";
        try {
            return Try.ofSupplier(decoratedSupplier)
                    .recover(e -> {
                                logger.error(tag + ": " + e.getMessage());
                                return handleException(e);
                            }
                    ).get();
        }catch (ReyRuntimeException re) {
            throw new ReyInternalException(re.getCause());
        }

    }

    private T handleException(Throwable e) {
        throw new ReyRuntimeException(e);
    }

}
