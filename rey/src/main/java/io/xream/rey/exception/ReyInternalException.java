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
package io.xream.rey.exception;

import io.xream.rey.api.ReyHttpStatus;

/**
 * @author Sim
 */
public class ReyInternalException extends RuntimeException {

    private int status;
    private String traceId;
    private String stack;
    private String fallback;
    private String path;
    public int httpStatus(){
        return 0;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ReyInternalException(Throwable e) {
        super(e);
    }


    private ReyInternalException(String message){
        super(message);
    }

    public static ReyInternalException create(ReyHttpStatus reyHttpStatus, int status, String error,
                                              String stack,
                                              String fallback,
                                              String path,
                                              String traceId){
        if (reyHttpStatus == ReyHttpStatus.INTERNAL_SERVER_ERROR){
            return new InternalServerError(status,error,stack,fallback,path,traceId);
        }else if (reyHttpStatus == ReyHttpStatus.BAD_REQUEST){
            return new BadRequest(error,stack,fallback,path,traceId);
        }else {
            return new ToClient(status,error,stack,fallback,path,traceId);
        }
    }

    public static final class BadRequest extends ReyInternalException {

        private BadRequest(String message,String stack,String fallback,String path, String traceId) {
            super(message);
            super.setStatus(400);
            super.setStack(stack);
            super.setFallback(fallback);
            super.setPath(path);
            super.setTraceId(traceId);
        }

        public int httpStatus(){
            return ReyHttpStatus.BAD_REQUEST.getStatus();
        }

    }

    public static final class InternalServerError extends ReyInternalException {

        private InternalServerError(int status,String message,String stack,String fallback,String path, String traceId) {
            super(message);
            super.setStatus(status);
            super.setStack(stack);
            super.setFallback(fallback);
            super.setPath(path);
            super.setTraceId(traceId);
        }

        public int httpStatus(){
            return ReyHttpStatus.INTERNAL_SERVER_ERROR.getStatus();
        }

    }


    public static final class ToClient extends ReyInternalException {

        private ToClient(int status, String message,String stack, String fallback,String path, String traceId) {
            super(message);
            super.setStatus(status);
            super.setStack(stack);
            super.setFallback(fallback);
            super.setPath(path);
            super.setTraceId(traceId);
        }

        public int httpStatus(){
            return ReyHttpStatus.TO_CLIENT.getStatus();
        }

    }
}
