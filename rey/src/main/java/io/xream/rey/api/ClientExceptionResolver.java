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
package io.xream.rey.api;

import io.xream.rey.exception.ReyInternalException;

/**
 * @author Sim
 */
public interface ClientExceptionResolver {

    void convertNot200ToException(int status, String response) throws ReyInternalException;
    void handleException(Throwable e) throws ReyInternalException;
    FallbackHandler fallbackHandler();
    CircuitbreakerExceptionHandler circuitbreakerExceptionHandler();

    default String adaptJson(String str) {
        str = str.split(": ")[1].trim();
        str = str.replace("[","");
        str = str.replace("]","");
        if (! (str.endsWith("}") )) {
            if ((str.endsWith("null") || str.endsWith("\"") )) {
                str += "}";
            }else {
                str += "\"}";
            }
        }
        return str;
    }
}