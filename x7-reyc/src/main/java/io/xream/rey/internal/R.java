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

import io.xream.rey.api.GroupRouter;
import io.xream.rey.api.UrlParamed;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public class R {
    private String url;
    private Class<?> returnType;
    private Class<?> geneType;
    private Object[] args;
    private RequestMethod requestMethod;
    private MultiValueMap headers;
    private GroupRouter router;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Class<?> getGeneType() {
        return geneType;
    }

    public void setGeneType(Class<?> geneType) {
        this.geneType = geneType;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public MultiValueMap getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap headers) {
        this.headers = headers;
    }

    public GroupRouter getRouter() {
        return router;
    }

    public void setRouter(GroupRouter router) {
        this.router = router;
    }

    public static R build(String clzzName, String methodName, Object[] args) {
        ReyParsed parsed = ReyParser.get(clzzName);
        String url = parsed.getUrl();

        MethodParsed methodParsed = parsed.getMap().get(methodName);

        Objects.requireNonNull(methodParsed);

        url = url + methodParsed.getRequestMapping();

        List<Object> objectList = new ArrayList<>();
        MultiValueMap headers = new LinkedMultiValueMap();
        headers.addAll(methodParsed.getHeaders());
        if (args != null) {
            for (Object arg : args) {
                if (arg != null && arg instanceof UrlParamed) {
                    UrlParamed urlParamed = (UrlParamed) arg;
                    url = urlParamed.value();
                }else if (arg != null && arg instanceof MultiValueMap) {
                    headers.addAll((MultiValueMap)arg);
                } else {
                    objectList.add(arg);
                }
            }
        }
        args = objectList.toArray();

        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        RequestMethod requestMethod = methodParsed.getRequestMethod();

        R r = new R();
        r.setArgs(args);
        r.setRequestMethod(requestMethod);
        r.setReturnType(methodParsed.getReturnType());
        r.setGeneType(methodParsed.getGeneType());
        r.setUrl(url);
        r.setHeaders(headers);
        r.setRouter(parsed.getGroupRouter());
        return r;
    }

    @Override
    public String toString() {
        return "R{" +
                "url='" + url + '\'' +
                ", returnType=" + returnType +
                ", geneType=" + geneType +
                ", args=" + Arrays.toString(args) +
                ", requestMethod=" + requestMethod +
                ", headers=" + headers +
                '}';
    }

}
