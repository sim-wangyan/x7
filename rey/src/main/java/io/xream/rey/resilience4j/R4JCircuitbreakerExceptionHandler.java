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
package io.xream.rey.resilience4j;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xream.internal.util.ExceptionUtil;
import io.xream.rey.api.CircuitbreakerExceptionHandler;
import io.xream.rey.api.ReyHttpStatus;
import io.xream.rey.exception.ReyInternalException;

/**
 * @author Sim
 */
public class R4JCircuitbreakerExceptionHandler implements CircuitbreakerExceptionHandler {

    public void handle(Throwable e) {
        if (e instanceof CallNotPermittedException) {//503
            throw ReyInternalException.create(ReyHttpStatus.TO_CLIENT, 503, e.getMessage(), ExceptionUtil.getStack(e), null, null, null);
        }
    }
}
