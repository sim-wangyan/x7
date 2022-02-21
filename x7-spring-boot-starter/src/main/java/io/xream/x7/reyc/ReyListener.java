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
package io.xream.x7.reyc;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.opentracing.Tracer;
import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import io.xream.x7.reyc.api.ReyTemplate;
import io.xream.x7.reyc.api.custom.RestTemplateCustomizer;
import io.xream.x7.reyc.internal.ClientBackendImpl;
import io.xream.x7.reyc.internal.R4JTemplate;
import io.xream.x7.reyc.internal.RestTemplateWrapper;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

public class ReyListener implements
        ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        customizeRestTemplate(event);
        wrap(event);
    }

    private void customizeRestTemplate(ApplicationStartedEvent event) {

        try {
            RestTemplateCustomizer bean = event.getApplicationContext().getBean(RestTemplateCustomizer.class);
            if (bean == null)
                return;
            RestTemplateWrapper wrapper = bean.customize();
            if (wrapper != null){
                ClientBackendImpl clientBackend = event.getApplicationContext().getBean(ClientBackendImpl.class);
                clientBackend.setRestTemplateWrapper(wrapper);
            }

        }catch (Exception e) {

        }

    }

    private void wrap(ApplicationStartedEvent event){
        try{
            RestTemplate restTemplate = restTemplate(event);
            RestTemplateWrapper wrapper = event.getApplicationContext().getBean(RestTemplateWrapper.class);
            wrapper.wrap(restTemplate);
            wrapR4jTemplate(event);
            headerInterceptor(wrapper,event);
        }catch (Exception e) {

        }
    }

    private void headerInterceptor(RestTemplateWrapper wrapper, ApplicationStartedEvent event) {
        try{
            Tracer tracer = event.getApplicationContext().getBean(Tracer.class);
            ClientHeaderInterceptor clientHeaderInterceptor = new TracingClientHeaderInterceptor(tracer);
            wrapper.headerInterceptor(clientHeaderInterceptor);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void wrapR4jTemplate(ApplicationStartedEvent event) {

        try{
            CircuitBreakerRegistry circuitBreakerRegistry = event.getApplicationContext().getBean(CircuitBreakerRegistry.class);
            RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
            ReyTemplate reyTemplate = event.getApplicationContext().getBean(ReyTemplate.class);
            R4JTemplate r4jTemplate = (R4JTemplate) reyTemplate;
            r4jTemplate.wrap(circuitBreakerRegistry,retryRegistry);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private RestTemplate restTemplate(ApplicationStartedEvent applicationStartedEvent) {
        try {
            RestTemplate restTemplate = applicationStartedEvent.getApplicationContext().getBean(RestTemplate.class);
            return restTemplate;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
