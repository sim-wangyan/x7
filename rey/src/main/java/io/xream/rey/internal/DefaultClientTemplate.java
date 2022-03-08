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
package io.xream.rey.internal;

import io.xream.internal.util.JsonX;
import io.xream.internal.util.LoggerProxy;
import io.xream.internal.util.StringUtil;
import io.xream.rey.api.ClientHeaderInterceptor;
import io.xream.rey.api.ClientTemplate;
import io.xream.rey.proto.ResponseString;
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
public class DefaultClientTemplate implements ClientTemplate {

    private RestTemplate restTemplate;

    private List<ClientHeaderInterceptor> clientHeaderInterceptorList = new ArrayList<>();


    @Override
    public void wrap(Object impl) {
        this.restTemplate = (RestTemplate)impl;
    }

    @Override
    public void headerInterceptor(ClientHeaderInterceptor interceptor){
        this.clientHeaderInterceptorList.add(interceptor);
    }

    @Override
    public ResponseString exchange(Class clz, String url, Object request, MultiValueMap headers, RequestMethod requestMethod) {
        ResponseString result = null;
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

        return result;
    }


    private ResponseString execute(Class clz, String url, Object request, MultiValueMap headerMap, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        if (headerMap != null) {
            headers.addAll(headerMap);
        }

        for (ClientHeaderInterceptor headerInterceptor : clientHeaderInterceptorList) {
            headerInterceptor.apply(headers);
        }

        // check content type
        if (headers.getContentType() == null
        && (
                method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.DELETE
                || method == HttpMethod.PATCH
                )) {
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

        ResponseString responseString = new ResponseString();
        responseString.setBody(re.getBody());
        responseString.setStatus(re.getStatusCodeValue());
        return responseString;

    }


}
