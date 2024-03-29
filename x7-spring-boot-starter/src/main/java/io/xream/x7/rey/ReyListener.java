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
package io.xream.x7.rey;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.opentracing.Tracer;
import io.xream.rey.api.*;
import io.xream.rey.api.custom.ClientExceptionResolverCustomizer;
import io.xream.rey.api.custom.RestTemplateCustomizer;
import io.xream.rey.internal.*;
import io.xream.rey.resilience4j.R4JCircuitbreakerExceptionHandler;
import io.xream.rey.resilience4j.R4JTemplate;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

public class ReyListener implements
        ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        ClientExceptionHandler handler = clientExceptionHandler(event);
        if (handler == null)
            return;
        customizeClientExceptionResolver(event, handler);
        customizeRestTemplate(event);
        wrap(event);
    }


    private ClientExceptionHandler clientExceptionHandler(ApplicationStartedEvent event){
        try{
            ClientExceptionHandler handler = event
                    .getApplicationContext()
                    .getBean(ClientExceptionHandler.class);

            if (handler == null)
                return null;
            DefaultClientExceptionResolver defaultClientExceptionResolver = new DefaultClientExceptionResolver();

            handler.setClientExceptionResolver(defaultClientExceptionResolver);

            defaultClientExceptionResolver.setFallbackHandler(new DefaultFallbackHandler());

            try{
                FallbackHandler fallbackHandler = event.getApplicationContext()
                        .getBean(FallbackHandler.class);
                if (fallbackHandler != null) {
                    defaultClientExceptionResolver.setFallbackHandler(fallbackHandler);
                }
            }catch (Exception e){

            }

            defaultClientExceptionResolver.setCircuitbreakerExceptionHandler(new R4JCircuitbreakerExceptionHandler());

            try{
                CircuitbreakerExceptionHandler circuitbreakerExceptionHandler = event.getApplicationContext()
                        .getBean(CircuitbreakerExceptionHandler.class);
                if (circuitbreakerExceptionHandler != null) {
                    defaultClientExceptionResolver.setCircuitbreakerExceptionHandler(circuitbreakerExceptionHandler);
                }
            }catch (Exception e){

            }

            return handler;
        }catch (Exception e) {

        }
        return null;
    }

    private void customizeClientExceptionResolver(ApplicationStartedEvent event, ClientExceptionHandler handler) {

        try{
            ClientExceptionResolverCustomizer customizer = event.getApplicationContext().getBean(ClientExceptionResolverCustomizer.class);
            if (customizer == null)
                return;
            ClientExceptionResolver resolver = customizer.customize();
            if (resolver == null)
                return;
            handler.setClientExceptionResolver(resolver);

        }catch (Exception e) {

        }
    }

    private void customizeRestTemplate(ApplicationStartedEvent event) {

        try {
            RestTemplateCustomizer bean = event.getApplicationContext().getBean(RestTemplateCustomizer.class);
            if (bean == null)
                return;
            ClientTemplate wrapper = bean.customize();
            if (wrapper != null){
                ClientBackendImpl clientBackend = event.getApplicationContext().getBean(ClientBackendImpl.class);
                clientBackend.setClientTemplate(wrapper);
            }

        }catch (Exception e) {

        }

    }

    private void wrap(ApplicationStartedEvent event){
        try{
            RestTemplate restTemplate = restTemplate(event);
            ClientTemplate clientTemplate = event.getApplicationContext().getBean(ClientTemplate.class);
            clientTemplate.wrap(restTemplate);
            wrapR4jTemplate(event);
            headerInterceptor(clientTemplate,event);
        }catch (Exception e) {

        }
    }

    private void headerInterceptor(ClientTemplate wrapper, ApplicationStartedEvent event) {
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

            ClientBackend clientBackend = event.getApplicationContext().getBean(ClientBackend.class);
            ClientBackendImpl impl = (ClientBackendImpl) clientBackend;
            impl.setReyTemplate(reyTemplate);

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
