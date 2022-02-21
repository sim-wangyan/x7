package io.xream.x7.reyc.internal;

import io.xream.x7.base.api.BackendService;

/**
 * @Author Sim
 */
public interface ClientBackend {

    Object toObject(Class<?> returnType, Class<?> geneType, String result);

    String service(ClientDecoration clientDecoration, BackendService<String> backendService);

    String handle(R r, Class clz);

    String fallback(String intfName, String methodName, Object[] args);
}
