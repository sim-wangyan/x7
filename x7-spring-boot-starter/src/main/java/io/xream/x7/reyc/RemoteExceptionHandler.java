package io.xream.x7.reyc;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.xream.x7.base.exception.ReyException;
import io.xream.x7.base.web.RemoteExceptionProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;

@RestControllerAdvice
public class RemoteExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteExceptionHandler.class);
    @Resource
    private Tracer tracer;

    @ExceptionHandler({
            ReyException.class
    })
    @ResponseBody
    public ResponseEntity<RemoteExceptionProto> handlerDemoteResourceAccessException(ReyException exception){

        Span span = tracer.scopeManager().activeSpan();
        String traceId = span == null ? "" : span.context().toTraceId()+ ":" + span.context().toSpanId();

        RemoteExceptionProto proto = new RemoteExceptionProto(exception,traceId);
                logger.error(proto.toJson());
        return ResponseEntity.status(exception.httpStatus()).body(
                proto
        );
    }


}
