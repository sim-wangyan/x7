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
package io.xream.x7.fallback.internal;

import io.xream.x7.annotation.Fallback;
import io.xream.x7.base.exception.FallbackUnexpectedReturnTypeException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;


/**
 * @author Sim
 */
@Aspect
public class FallbackAspect implements io.xream.x7.fallback.Fallback {

    private final static Logger logger = LoggerFactory.getLogger(Fallback.class);

    public FallbackAspect() {
        logger.info("Fallback Enabled");
    }

    @Pointcut("@annotation(io.xream.x7.annotation.Fallback))")
    public void cut() {
    }

    @Around("cut() && @annotation(fallback) ")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, Fallback fallback) {

        Object[] args = proceedingJoinPoint.getArgs();

        Signature signature = proceedingJoinPoint.getSignature();

        Method method = null;

        try {
            MethodSignature ms = ((MethodSignature) signature);
            method = ms.getMethod();
            if (ms.getReturnType() == void.class) {
                proceedingJoinPoint.proceed();
                return null;
            } else {
                return proceedingJoinPoint.proceed();
            }
        } catch (Throwable e) {

            Class fallbackClzz = fallback.fallback();
            if (fallbackClzz != void.class) {
                Class<? extends Throwable>[] exceptionClzzArr = fallback.exceptions();

                for (Class ec : exceptionClzzArr) {
                    if (e.getClass() == ec || e.getClass().isAssignableFrom(ec)) {
                        return fallback(FallbacKey.of(method), args,e);
                    }
                }
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }else {
                throw new FallbackUnexpectedReturnTypeException(e);
            }

        }

    }

}
