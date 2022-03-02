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
package io.xream.x7.reyc;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.BizException;
import io.xream.x7.base.exception.FallbackUnexpectedReturnTypeException;
import io.xream.x7.base.exception.ReyBizException;
import io.xream.x7.base.util.ExceptionUtil;
import io.xream.x7.base.web.RemoteExceptionProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;

@RestControllerAdvice
public class DefaultExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    @Resource
    private Tracer tracer;

    @ExceptionHandler({
            NullPointerException.class,
            IllegalArgumentException.class,
            BizException.class,
            RuntimeException.class
    })
    @ResponseBody
    public ResponseEntity<RemoteExceptionProto> handleDefaultException(RuntimeException e){

        final String stack = ExceptionUtil.getMessage(e);
        logger.error(stack);

        if (e.getClass().getName().startsWith("org.springframework.http"))
            throw e;

        Span span = tracer.scopeManager().activeSpan();
        String traceId = span == null ? "" : span.context().toTraceId()+ ":" + span.context().toSpanId();

        int status = 500;
        String message = null;
        if (e instanceof NullPointerException){
            message = stack;
        }else if (e instanceof FallbackUnexpectedReturnTypeException){
            throw e;
        }else if (e instanceof ReyBizException){
            throw e;
        }else {
            message = e.getMessage();
        }

        return ResponseEntity.status(ReyHttpStatus.INTERNAL_SERVER_ERROR.getStatus()).body(
                new RemoteExceptionProto(status,message,stack,traceId)
        );
    }


}
