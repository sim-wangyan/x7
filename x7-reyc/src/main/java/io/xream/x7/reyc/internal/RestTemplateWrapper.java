package io.xream.x7.reyc.internal;

import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import org.springframework.util.MultiValueMap;

/**
 * @Author Sim
 */
public interface RestTemplateWrapper {

    void wrap(Object impl);
    void headerInterceptor(ClientHeaderInterceptor interceptor);
    String get(Class clz, String url, MultiValueMap headers);
    String post(Class clz, String url, Object request, MultiValueMap headers);
    String put(Class clz, String url, Object request, MultiValueMap headers);
    String delete(Class clz, String url, Object request, MultiValueMap headers);
}
