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
package io.xream.rey.proto;

import io.xream.internal.util.JsonX;
import io.xream.internal.util.StringUtil;
import io.xream.rey.api.ReyHttpStatus;
import io.xream.rey.exception.ReyInternalException;

/**
 * @author Sim
 */
public class RemoteExceptionProto {

    private int status;
    private String traceId;
    private String error;
    private String stack;
    private String path;
    private String fallback;

    public RemoteExceptionProto(){
    }

    public RemoteExceptionProto(ReyInternalException exception, String traceId){
        this.status = exception.getStatus();
        this.error = exception.getMessage();
        this.stack = exception.getStack();
        this.traceId = StringUtil.isNullOrEmpty(exception.getTraceId()) ? traceId : exception.getTraceId();
        this.fallback = exception.getFallback();
        this.path = exception.getPath();
    }

    public RemoteExceptionProto(int status, String error, String statck, String traceId){
        this.status = status;
        this.error = error;
        this.stack = statck;
        this.traceId = traceId;
    }

    public ReyInternalException create(ReyHttpStatus reyHttpStatus) {
        return ReyInternalException.create(reyHttpStatus,this.status,this.error,this.stack,this.fallback,this.path, this.traceId);
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String toJson(){
        return JsonX.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
