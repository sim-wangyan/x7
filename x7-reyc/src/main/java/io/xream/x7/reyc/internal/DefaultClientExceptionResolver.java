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

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.ReyBizException;
import io.xream.x7.base.exception.ReyInternalException;
import io.xream.x7.base.util.ExceptionUtil;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.web.RemoteExceptionProto;
import io.xream.x7.reyc.api.ClientExceptionResolver;
import io.xream.x7.reyc.api.FallbackHandler;
import org.apache.commons.collections.MapUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

/**
 * @author Sim
 */
public class DefaultClientExceptionResolver implements ClientExceptionResolver {

    private FallbackHandler fallbackHandler;

    public void setFallbackHandler(FallbackHandler fallbackHandler) {
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public void convertNot200ToException(int status, String response) throws ReyInternalException{
        if (status == ReyHttpStatus.TO_CLIENT.getStatus()) {
            RemoteExceptionProto proto = JsonX.toObject(response, RemoteExceptionProto.class);
            throw proto.create(ReyHttpStatus.TO_CLIENT);
        }
    }

    @Override
    public void handleException(Throwable e) throws ReyInternalException{

        if (e instanceof CallNotPermittedException) {//503
            throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, 503 ,e.getMessage(), ExceptionUtil.getStack(e),null,null,null);
        }

        if (e instanceof ResourceAccessException){
            ResourceAccessException rae = (ResourceAccessException)e;
            Throwable t = rae.getRootCause();
            String str = rae.getLocalizedMessage();
            String[] arr = str.split(";");
            final String message = arr[0];
            if (t instanceof ConnectException) {
                throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, -1 ,message, ExceptionUtil.getStack(e),null,null,null);
            }else if (t instanceof SocketTimeoutException) {
                throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, -2 ,message,ExceptionUtil.getStack(e),null,null,null);
            }
        }else if (e instanceof HttpClientErrorException){
            HttpClientErrorException ee = (HttpClientErrorException)e;
            String str = ee.getLocalizedMessage();
            str = adaptJson(str);
            Map<String,Object> map = JsonX.toMap(str);
            String message = MapUtils.getString(map,"error");
            String path = MapUtils.getString(map, "path");
            String stack = ExceptionUtil.getStack(e);
            throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, ee.getStatusCode().value() ,message,stack,null,path,null);
        }else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException hse = (HttpServerErrorException)e;
            hse.printStackTrace();
            String str = hse.getLocalizedMessage();
            str = adaptJson(str);
            Map<String,Object> map = JsonX.toMap(str);
            String stack = MapUtils.getString(map,"stack");
            if (stack == null) {
                stack = ExceptionUtil.getStack(e);
            }
            String message = MapUtils.getString(map,"error");
            String path = MapUtils.getString(map,"path");
            throw ReyInternalException.create(ReyHttpStatus.INTERNAL_SERVER_ERROR, hse.getStatusCode().value() ,
                    message,
                    stack,
                    null,
                    path,
                    MapUtils.getString(map,"traceId")
            );
        }else if (e instanceof IllegalArgumentException) {
            throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, 400, e.getMessage(), ExceptionUtil.getStack(e),null,null,null);
        }

        throw new ReyBizException(e);
    }

    @Override
    public FallbackHandler fallbackHandler() {
        return this.fallbackHandler;
    }

}
