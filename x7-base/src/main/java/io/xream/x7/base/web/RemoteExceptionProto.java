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
package io.xream.x7.base.web;

import io.xream.x7.base.api.ExceptionTyped;
import io.xream.x7.base.exception.*;

/**
 * @Author Sim
 */
public class RemoteExceptionProto {

    private String type;
    private String traceId;
    private String message;

    public RemoteExceptionProto(){
    }

    public RemoteExceptionProto(RuntimeException e, String traceId) {
        if (e instanceof ExceptionTyped){
            this.type = ((ExceptionTyped) e).getType();
        }
        this.message = e.getMessage();
        this.traceId = traceId;
    }

    public RuntimeException exception(String message){
        if (this.type.equals(RemoteExceptionType.RESOURCE.name())){
            return new RemoteResourceAccessException(this.message);
        }else if (this.type.equals(RemoteExceptionType.BIZ_REMOTE.name())) {
            return new RemoteBizException(this.message);
        }else if (this.type.equals(RemoteExceptionType.REMOTE_BAD_REQUEST_400.name())) {
            return new RemoteBadRequestException(this.message);
        }else if (this.type.equals(RemoteExceptionType.REMOTE_NOT_FOUND_404.name())) {
            return new RemoteNotFoundException(this.message);
        }else if (this.type.equals(RemoteExceptionType.REMOTE_METHOD_NOT_ALLOWED_405.name())) {
            return new RemoteMethodNotAllowedException(this.message);
        }else if (this.type.equals(RemoteExceptionType.REMOTE_CONNECT_FAILED_502.name())) {
            return new RemoteConnectionException(this.message);
        }else if (this.type.equals(RemoteExceptionType.REMOTE_UNAVAILABLE_503.name())) {
            return new RemoteUnavailableException(this.message);
        }
        else if (this.type.equals(RemoteExceptionType.REMOTE_TIMEOUT_504.name())) {
            return new RemoteTimeoutException(this.message);
        }
        throw new RemoteUnexpectedException(message);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
