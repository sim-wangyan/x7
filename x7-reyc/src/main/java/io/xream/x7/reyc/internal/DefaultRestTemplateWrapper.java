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

import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.RemoteBizException;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.util.LoggerProxy;
import io.xream.x7.base.util.StringUtil;
import io.xream.x7.base.web.RemoteExceptionProto;
import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sim
 */
public class DefaultRestTemplateWrapper implements RestTemplateWrapper {

    private RestTemplate restTemplate;

    private List<ClientHeaderInterceptor> clientHeaderInterceptorList = new ArrayList<>();

    private ClientExceptionHandler clientExceptionHandler;

    public DefaultRestTemplateWrapper(ClientExceptionHandler handler) {
        this.clientExceptionHandler = handler;
    }


    @Override
    public void wrap(Object impl) {
        this.restTemplate = (RestTemplate)impl;
    }

    @Override
    public void headerInterceptor(ClientHeaderInterceptor interceptor){
        this.clientHeaderInterceptorList.add(interceptor);
    }

    @Override
    public String exchange(Class clz, String url, Object request, MultiValueMap headers, RequestMethod requestMethod) {
        String result = null;
        switch (requestMethod) {
            case GET:
                result = this.execute(clz,url,request,headers,HttpMethod.GET);
                break;
            case PUT:
                result = this.execute(clz,url,request,headers,HttpMethod.PUT);
                break;
            case DELETE:
                result = this.execute(clz,url,request,headers,HttpMethod.DELETE);
                break;
            default:
                result = this.execute(clz,url,request,headers,HttpMethod.POST);
        }
        if (result.contains("Internal Server Error")
                || result.contains("not support")
                || result.contains("Unknown Source")                || result.contains("Exception")
                || result.contains("Throwable"))
            throw new RemoteBizException(requestMethod + " " +url + " response:" + result + " RemoteException end    ");
        return result;
    }


    private String execute(Class clz, String url, Object request, MultiValueMap headerMap, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        if (headerMap != null) {
            headers.addAll(headerMap);
        }

        for (ClientHeaderInterceptor headerInterceptor : clientHeaderInterceptorList) {
            headerInterceptor.apply(headers);
        }

        // check content type
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        StringBuilder headerStr = new StringBuilder();

        headers.entrySet().stream().forEach(
                header ->  headerStr.append(" -H ").append(header.getKey()).append(":").append(header.getValue().stream().collect(Collectors.joining()))
        );

        String json = request == null ? "" : JsonX.toJson(request);

        LoggerProxy.info(clz, "-X " + method.name() + "  " + url + headerStr + (StringUtil.isNotNull(json) ? (" -d '" + json + "'"):""));

        if (this.restTemplate == null)
            throw new NullPointerException(RestTemplate.class.getName());

        ResponseEntity<String> re = restTemplate.exchange(url, method, new HttpEntity<>(json, headers), String.class);

        final String body = re.getBody();

        if (! isIgnore222()) {
            final int status = re.getStatusCodeValue();
            if (status == ReyHttpStatus.TO_CLIENT.getStatus()) {
                RemoteExceptionProto proto = JsonX.toObject(body, RemoteExceptionProto.class);
                throw proto.create(ReyHttpStatus.TO_CLIENT);
            }
        }

        return body;

    }

    private boolean isIgnore222(){
        return this.clientExceptionHandler.resolver().ignore222();
    }

}
