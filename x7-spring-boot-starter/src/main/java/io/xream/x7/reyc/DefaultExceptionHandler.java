package io.xream.x7.reyc;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.xream.x7.base.api.ReyHttpStatus;
import io.xream.x7.base.exception.BizException;
import io.xream.x7.base.exception.RemoteBizException;
import io.xream.x7.base.util.ExceptionUtil;
import io.xream.x7.base.web.RemoteExceptionProto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;

@RestControllerAdvice
public class DefaultExceptionHandler {

    @Resource
    private Tracer tracer;

    @ExceptionHandler({
            NullPointerException.class,
            IllegalArgumentException.class,
            BizException.class,
            RemoteBizException.class
    })
    @ResponseBody
    public ResponseEntity<RemoteExceptionProto> handleDefaultException(RuntimeException e){

        Span span = tracer.scopeManager().activeSpan();
        String traceId = span == null ? "" : span.context().toTraceId()+ ":" + span.context().toSpanId();

        int status = 500;
        String message = null;
        String stack = null;
        if (e instanceof NullPointerException){
            message = ExceptionUtil.getMessage(e);
        }else {
            message = e.getMessage();
            stack = ExceptionUtil.getMessage(e);
        }

        return ResponseEntity.status(ReyHttpStatus.INTERNAL_SERVER_ERROR.getStatus()).body(
                new RemoteExceptionProto(status,message,stack,traceId)
        );
    }


}
