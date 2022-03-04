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
package io.xream.x7.fallback.internal.aop;

import io.xream.x7.fallback.Fallback;
import io.xream.x7.fallback.internal.FallbacKey;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author Sim
 */
public class FallbackInterceptor implements MethodInterceptor, Fallback {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Method method = methodInvocation.getMethod();
        System.out.println("MethodInvocation.method: " + method.toString());
        Object[] args = methodInvocation.getArguments();
        Class rc = method.getReturnType();
        try{
            if (rc == void.class){
                methodInvocation.proceed();
                return null;
            }
            return methodInvocation.proceed();
        }catch (Exception e){
            return fallback(FallbacKey.of(method),args,e);
        }
    }

}
