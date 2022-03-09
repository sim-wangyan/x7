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
package io.xream.rey.fallback;

import io.xream.rey.exception.ReyInternalException;
import io.xream.rey.fallback.monitor.FallbackCounter;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Sim
 */
public interface Fallback {

    default Object fallback(FallbacKey key, Object[] args, Throwable ignoredException) throws Throwable{
        FallbackParsed parsed = FallbackParser.get(key);

        if (parsed == null) {
            throw ignoredException;
        }
        if (ignoredException instanceof ReyInternalException.BadRequest){
            throw ignoredException;
        }
        boolean isNotRequiredCatch = false;
        Class[] es = parsed.getIgnoreExceptions();
        if (es != null && es.length > 0) {
            for (Class ec: parsed.getIgnoreExceptions()) {
                if (ec == ignoredException.getClass() || ec.isAssignableFrom(ignoredException.getClass())) {
                    isNotRequiredCatch = true;
                    break;
                }
            }
        }
        if (isNotRequiredCatch) {
            throw ignoredException;
        }
        try {
            String uri = ignoredException instanceof ReyInternalException ? ((ReyInternalException) ignoredException).getPath() :"";
            String exceptionName = ignoredException.getClass().getSimpleName();
            FallbackCounter.increment(parsed.getTargetClass().getSimpleName(),parsed.getMethod().getName(),uri,exceptionName);
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
            throw tte.getTargetException();
        }catch (Exception ee) {
            throw ee;
        }
    }

}
