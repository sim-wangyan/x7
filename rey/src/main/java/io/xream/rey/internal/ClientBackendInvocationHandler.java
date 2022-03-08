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

import io.xream.internal.util.LoggerProxy;
import io.xream.rey.api.BackendService;
import io.xream.rey.exception.MismatchedReturnTypeException;
import io.xream.rey.exception.ReyInternalException;
import io.xream.rey.fallback.FallbacKey;
import io.xream.rey.proto.ResponseString;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Sim
 */
public class ClientBackendInvocationHandler implements InvocationHandler {

    private ClientBackendProxy clientBackendProxy;

    private ClientBackend getBackend(){
        return clientBackendProxy.getClientBackend();
    }

    public ClientBackendInvocationHandler(ClientBackendProxy clientBackendProxy){
        this.clientBackendProxy = clientBackendProxy;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        if (methodName.equals("toString"))
            return null;

        Class clzz = clientBackendProxy.getObjectType();
        long startTime = System.currentTimeMillis();
        final String clzzName = clzz.getName();
        try{

            ClientBackend clientBackend = getBackend();

            LoggerProxy.debug(clzz,methodName +"(..) start....");

            R r = R.build(clzzName,methodName,args);

            BackendDecoration backendDecoration = clientBackendProxy.getBackendDecoration();

            if (backendDecoration.getConfigName() == null) {
                ResponseString result = getBackend().handle(r,clzz);
                if (result == null)
                    return null;
                return clientBackend.toObject(r.getReturnType(),r.getGeneType(),result.getBody());
            }

            BackendDecoration cd = clientBackendProxy.isReyTemplateNotRequired() ? null : clientBackendProxy.getBackendDecoration();

            Object result = clientBackend.service(cd, new BackendService<ResponseString>() {
                @Override
                public ResponseString handle() {
                    return clientBackend.handle(r,clzz);
                }

                @Override
                public Object fallback(Throwable e) throws Throwable{
                    return clientBackend.fallback(FallbacKey.of(method),args,e);
                }
            });

            if (result == null)
                return null;

            if (result instanceof ResponseString) {
                ResponseString responseString = (ResponseString) result;
                return clientBackend.toObject(r.getReturnType(), r.getGeneType(), responseString.getBody());
            }else if (result.getClass() == r.getReturnType()){
                return result;
            }else {
                throw new MismatchedReturnTypeException("FALLBACK AND GET MISMATCHED RESULT, " +
                        "catch and invoke e.getTag() to handle",result);
            }
        } catch (RuntimeException rie){
            Throwable throwable = rie.getCause();
            if (throwable != null && throwable instanceof ReyInternalException) {
                throw rie.getCause();
            }
            throw rie;
        } finally{
            long endTime = System.currentTimeMillis();
            LoggerProxy.debug(clzz,methodName + "(..) end, cost time: " + (endTime - startTime) + "ms");
        }
    }
}
