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
import io.xream.x7.base.util.ExceptionUtil;
import io.xream.x7.base.util.LoggerProxy;
import io.xream.x7.base.web.ResponseString;

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
    public Object invoke(Object proxy, Method method, Object[] args)  {
        final String methodName = method.getName();
        if (methodName.equals("toString"))
            return null;

        Class clzz = clientBackendProxy.getObjectType();
        long startTime = System.currentTimeMillis();
        try{

            ClientBackend clientBackend = getBackend();

            LoggerProxy.debug(clzz,methodName +"(..) start....");

            R r = R.build(clzz.getName(),methodName,args);

            ClientDecoration clientDecoration = clientBackendProxy.getClientDecoration();

            if (clientDecoration.getBackendName() == null) {
                ResponseString result = getBackend().handle(r,clzz);
                if (result == null)
                    return null;
                return clientBackend.toObject(r.getReturnType(),r.getGeneType(),result.getBody());
            }

            String result = clientBackend.service(clientBackendProxy.getClientDecoration(), new BackendService<Object>() {
                @Override
                public Object handle() {
                    return clientBackend.handle(r,clzz);
                }

                @Override
                public Object fallback() {
                    return clientBackend.fallback(clzz.getName(),methodName,args);
                }
            });

            return clientBackend.toObject(r.getReturnType(),r.getGeneType(),result);

        } catch (RuntimeException re){
            throw re;
        } catch (Exception e){
            throw new RuntimeException(ExceptionUtil.getMessage(e));
        }finally {
            long endTime = System.currentTimeMillis();
            LoggerProxy.debug(clzz,methodName + "(..) end, cost time: " + (endTime - startTime) + "ms");
        }
    }
}
