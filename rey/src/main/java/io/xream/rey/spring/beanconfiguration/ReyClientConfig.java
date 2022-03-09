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

import io.xream.rey.api.ClientTemplate;
import io.xream.rey.internal.ClientBackend;
import io.xream.rey.internal.ClientBackendImpl;
import io.xream.rey.internal.ClientExceptionHandler;
import io.xream.rey.internal.DefaultClientTemplate;
import org.springframework.context.annotation.Bean;

/**
 * @author Rolyer Luo
 */
public class ReyClientConfig  {

    @Bean
    public ClientTemplate clientTemplate() {
        return new DefaultClientTemplate();
    }
    @Bean
    public ClientBackend clientBackend(ClientExceptionHandler clientExceptionHandler, ClientTemplate wrapper)  {

        ClientBackendImpl clientBackend = new ClientBackendImpl(wrapper);
        clientBackend.setClientExceptionHandler(clientExceptionHandler);
        return clientBackend;
    }

}
