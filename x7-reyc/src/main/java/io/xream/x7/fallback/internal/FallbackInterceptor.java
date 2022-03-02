package io.xream.x7.fallback.internal;

import io.xream.x7.fallback.Fallback;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @Author Sim
 */
public class FallbackInterceptor implements MethodInterceptor, Fallback {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Method method = methodInvocation.getMethod();
        System.out.println("MethodInvocation.method: " + method.toString());
        Object[] args = methodInvocation.getArguments();
        Class rc = method.getReturnType();
        try{
            if (rc == void.class){
                methodInvocation.proceed();
                return null;
            }
            return methodInvocation.proceed();
        }catch (Exception e){
            return fallback(FallbacKey.of(method),args,e);
        }
    }

}
