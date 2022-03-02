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
package io.xream.x7.fallback;

import io.xream.x7.base.exception.ReyBizException;
import io.xream.x7.fallback.internal.FallbacKey;
import io.xream.x7.fallback.internal.FallbackParsed;
import io.xream.x7.fallback.internal.FallbackParser;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Sim
 */
public interface Fallback {

    default Object fallback(FallbacKey key, Object[] args, Throwable e) {
        FallbackParsed parsed = FallbackParser.get(key);

        if (parsed == null) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }else {
                throw new ReyBizException(e);
            }
        }
        boolean isCatchRequired = false;
        Class[] es = parsed.getExceptions();
        if (es != null && es.length > 0) {
            for (Class ec: parsed.getExceptions()) {
                if (e.getClass() == ec || e.getClass().isAssignableFrom(ec)) {
                    isCatchRequired = true;
                    break;
                }
            }
        }
        if (isCatchRequired) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }else {
                throw new ReyBizException(e);
            }
        }
        try {
            if (parsed.getMethod().getReturnType() == void.class) {
                if (args == null || args.length == 0) {
                    parsed.getMethod().invoke(parsed.getFallback());
                }else {
                    parsed.getMethod().invoke(parsed.getFallback(), args);
                }
                return null;
            }
            if (args == null || args.length == 0) {
                return parsed.getMethod().invoke(parsed.getFallback());
            }else {
                return parsed.getMethod().invoke(parsed.getFallback(), args);
            }
        }catch (InvocationTargetException tte){
            Throwable t = tte.getTargetException();
            if (t instanceof RuntimeException){
                throw (RuntimeException) t;
            }else {
                throw new RuntimeException(t);
            }
        }catch (Exception ee) {
            if (ee instanceof RuntimeException){
                throw (RuntimeException) ee;
            }else {
                throw new RuntimeException(ee);
            }
        }
    }

}
