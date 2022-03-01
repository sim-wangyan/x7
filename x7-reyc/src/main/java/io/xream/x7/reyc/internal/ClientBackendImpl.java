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

import io.xream.x7.base.api.BackendService;
import io.xream.x7.base.api.GroupRouter;
import io.xream.x7.base.exception.ReyBizException;
import io.xream.x7.base.exception.ReyInternalException;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.util.StringUtil;
import io.xream.x7.base.web.ResponseString;
import io.xream.x7.reyc.api.ReyTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Sim
 */
public class ClientBackendImpl implements ClientBackend {


    private ClientExceptionHandler clientExceptionHandler;

    private ReyTemplate reyTemplate;

    private RestTemplateWrapper restTemplateWrapper;

    public ClientBackendImpl(RestTemplateWrapper wrapper) {
        this.restTemplateWrapper = wrapper;
    }

    @Override
    public void setClientExceptionHandler(ClientExceptionHandler clientExceptionHandler) {
        this.clientExceptionHandler = clientExceptionHandler;
    }

    public void setRestTemplateWrapper(RestTemplateWrapper restTemplate) {
        this.restTemplateWrapper = restTemplate;
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

        ResponseString result = null;
        if (requestMethod == RequestMethod.GET) {
            List<String> regExList = StringUtil.listByRegEx(url, pattern);
            int size = regExList.size();
            for (int i = 0; i < size; i++) {
                url = url.replace(regExList.get(i), args[i].toString());
            }
            result = restTemplateWrapper.exchange(clz,url,null,headers,requestMethod);

        } else {
            if (args != null && args.length > 0) {
                result = restTemplateWrapper.exchange(clz,url,args[0],headers,requestMethod);
            } else {
                result = restTemplateWrapper.exchange(clz,url,null,headers,requestMethod);
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
    public String service(BackendDecoration backendDecoration, BackendService<Object> backendService) throws ReyInternalException {

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
                    Object fallback = backendService.fallback();
                    if (fallback != null) {
                        final String tag = "Backend(" + backendDecoration.getServiceName() + ")";
                        throw new ReyBizException(tag + " FALLBACK", fallback);
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

        return body;
    }

    @Override
    public Object fallback(String intfName, String methodName, Object[] args) {

        ClientParsed parsed = ClientParser.get(intfName);
        if (parsed.getFallback() == null)
            return null;
        Method method = parsed.getFallbackMethodMap().get(methodName);

        if (method == null)
            return null;
        try {
            if (method.getReturnType() == void.class) {
                method.invoke(parsed.getFallback(), args);
                return null;
            }
            return method.invoke(parsed.getFallback(), args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception of fallback: " + intfName + "." + methodName);
        }

    }

}
