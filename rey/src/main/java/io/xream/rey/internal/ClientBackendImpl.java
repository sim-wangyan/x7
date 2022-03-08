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

import io.xream.internal.util.ExceptionUtil;
import io.xream.internal.util.JsonX;
import io.xream.internal.util.StringUtil;
import io.xream.rey.api.BackendService;
import io.xream.rey.api.ClientTemplate;
import io.xream.rey.api.GroupRouter;
import io.xream.rey.api.ReyTemplate;
import io.xream.rey.exception.ReyInternalException;
import io.xream.rey.proto.ResponseString;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Sim
 */
public class ClientBackendImpl implements ClientBackend {


    private ClientExceptionHandler clientExceptionHandler;

    private ReyTemplate reyTemplate;

    private ClientTemplate clientTemplate;

    public ClientBackendImpl(ClientTemplate wrapper) {
        this.clientTemplate = wrapper;
    }

    @Override
    public void setClientExceptionHandler(ClientExceptionHandler clientExceptionHandler) {
        this.clientExceptionHandler = clientExceptionHandler;
    }

    public void setClientTemplate(ClientTemplate restTemplate) {
        this.clientTemplate = restTemplate;
    }

    public void setReyTemplate( ReyTemplate reyTemplate) {
        this.reyTemplate = reyTemplate;
    }

    private Pattern pattern = Pattern.compile("\\{[\\w]*\\}");


    @Override
    public ResponseString handle(R r, Class clz) {

        RequestMethod requestMethod = r.getRequestMethod();
        Object[] args = r.getArgs();
        String url = r.getUrl();
        MultiValueMap headers = r.getHeaders();

        GroupRouter router = r.getRouter();
        if (router != null){
            Object arg = null;
            if (args != null && args.length > 0) {
                arg = args[0];
            }
            url = url.replace(router.replaceHolder(),router.replaceValue(arg));
        }

        if (url.contains("{")) {
            List<String> regExList = StringUtil.listByRegEx(url, pattern);
            int size = regExList.size();
            for (int i = 0; i < size; i++) {
                url = url.replace(regExList.get(i), args[i].toString());
            }
        }

        ResponseString result = null;
        if (requestMethod == RequestMethod.GET) {
            result = clientTemplate.exchange(clz,url,null,headers,requestMethod);
        } else {
            if (args != null && args.length > 0) {
                result = clientTemplate.exchange(clz,url,args[0],headers,requestMethod);
            } else {
                result = clientTemplate.exchange(clz,url,null,headers,requestMethod);
            }
        }

        return result;
    }

    @Override
    public Object toObject(Class<?> returnType, Class<?> geneType, String result) {

        if (StringUtil.isNullOrEmpty(result))
            return null;

        if (returnType == null || returnType == void.class) {
            return null;
        }

        if (returnType == Object.class)
            return result;

        if (returnType == List.class){
            return JsonX.toList(result,geneType);
        }

        return JsonX.toObject(result, returnType);
    }

    @Override
    public Object service(BackendDecoration backendDecoration, BackendService<ResponseString> backendService) throws ReyInternalException {

        Object result = null;
        try {
            if (backendDecoration == null || reyTemplate == null) {
                try {
                    result = backendService.handle();
                }catch (Exception e) {
                    throw new ReyInternalException(e) {
                        @Override
                        public int httpStatus() {
                            return 0;
                        }
                    };
                }
            } else {
                result = reyTemplate.support(
                        backendDecoration.getServiceName(),
                        backendDecoration.getConfigName(), backendDecoration.isRetry(),
                        backendService
                );
            }
        }catch (ReyInternalException e) {
            try {
                this.clientExceptionHandler.resolver().handleException(e.getCause());
            }catch (ReyInternalException rie) {

                if (! this.clientExceptionHandler.resolver()
                        .fallbackHandler().isNotRequireFallback(rie.getStatus())) {
                    try {
                        return backendService.fallback(rie);
                    }catch (Throwable t) {
                        if (t instanceof ReyInternalException)
                            throw rie;
                        rie.setFallback(ExceptionUtil.getMessage(t));
                        throw rie;
                    }
                }

                throw rie;
            }
        }catch (Exception e) {
            throw e;
        }

        if (result == null)
            return null;
        ResponseString responseString = (ResponseString) result;
        final int status = responseString.getStatus();
        final String body = responseString.getBody();

        this.clientExceptionHandler.resolver().convertNot200ToException(status,body);

        return result;
    }

}
