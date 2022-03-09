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
package io.xream.rey.spring.beanconfiguration;

import io.micrometer.core.instrument.MeterRegistry;
import io.xream.rey.fallback.aop.FallbackInterceptor;
import io.xream.rey.fallback.monitor.FallbackCounter;
import org.springframework.context.annotation.Bean;

/**
 * @author Sim
 */
public class FallbackConfig {

    @Bean
    public FallbackInterceptor fallbackInterceptor(){
        return new FallbackInterceptor();
    }

    @Bean
    FallbackPostProcessor fallbackProxy(FallbackInterceptor fallbackInterceptor){

        FallbackPostProcessor fallbackPostProcessor = new FallbackPostProcessor();
        fallbackPostProcessor.setAdvice(fallbackInterceptor);
        return fallbackPostProcessor;
    }

    @Bean
    FallbackCounter fallbackCounter(MeterRegistry registry) {
        return new FallbackCounter(registry);
    }
}
