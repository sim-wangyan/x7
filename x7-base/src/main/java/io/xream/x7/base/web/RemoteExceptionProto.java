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

import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.ReyInternalException;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.util.StringUtil;

/**
 * @author Sim
 */
public class RemoteExceptionProto {

    private int status;
    private String traceId;
    private String message;
    private String stack;
    private String fallback;

    public RemoteExceptionProto(){
    }

    public RemoteExceptionProto(ReyInternalException exception, String traceId){
        this.status = exception.getStatus();
        this.message = exception.getMessage();
        this.stack = exception.getStack();
        this.traceId = StringUtil.isNullOrEmpty(exception.getTraceId()) ? traceId : exception.getTraceId();
        this.fallback = exception.getFallback();
    }

    public RemoteExceptionProto(int status, String message, String statck, String traceId){
        this.status = status;
        this.message = message;
        this.stack = statck;
        this.traceId = traceId;
        if (StringUtil.isNullOrEmpty(this.message)){
            this.message = this.stack;
            this.stack = null;
        }
    }

    public ReyInternalException create(ReyHttpStatus reyHttpStatus) {
        return ReyInternalException.create(reyHttpStatus,this.status,this.message,this.stack,this.fallback, this.traceId);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String toJson(){
        System.out.println(this.message);
        System.out.println(this.fallback);
        String str = JsonX.toJson(this);
        System.out.println(str);
        return str;
    }

    @Override
    public String toString() {
        return toJson();
    }
}
