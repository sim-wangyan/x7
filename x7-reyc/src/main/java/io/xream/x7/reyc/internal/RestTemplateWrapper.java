package io.xream.x7.reyc.internal;

import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author Sim
 */
public interface RestTemplateWrapper {

    void wrap(Object impl);
    void headerInterceptor(ClientHeaderInterceptor interceptor);
    String exchange(Class clz, String url, Object request, MultiValueMap headers, RequestMethod httpMethod);
}
