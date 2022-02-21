package io.xream.x7.reyc.internal;

import io.xream.x7.base.exception.RemoteServiceException;
import io.xream.x7.base.util.JsonX;
import io.xream.x7.base.util.LoggerProxy;
import io.xream.x7.base.util.StringUtil;
import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sim
 */
public class DefaultRestTemplateWrapper implements RestTemplateWrapper {

    private RestTemplate restTemplate;

    private List<ClientHeaderInterceptor> clientHeaderInterceptorList = new ArrayList<>();

    @Override
    public void wrap(Object impl) {
        this.restTemplate = (RestTemplate)impl;
    }

    @Override
    public void headerInterceptor(ClientHeaderInterceptor interceptor){
        this.clientHeaderInterceptorList.add(interceptor);
    }

    @Override
    public String exchange(Class clz, String url, Object request, MultiValueMap headers, RequestMethod requestMethod) {
        String result = null;
        switch (requestMethod) {
            case GET:
                result = this.execute(clz,url,request,headers,HttpMethod.GET);
                break;
            case PUT:
                result = this.execute(clz,url,request,headers,HttpMethod.PUT);
                break;
            case DELETE:
                result = this.execute(clz,url,request,headers,HttpMethod.DELETE);
                break;
            default:
                result = this.execute(clz,url,request,headers,HttpMethod.POST);
        }
        if (result.contains("Internal Server Error")
                || result.contains("not support")
                || result.contains("Unknown Source")
                || result.contains("Exception")
                || result.contains("Throwable"))
            throw new RemoteServiceException(requestMethod + " " +url + " response:" + result + " RemoteException end    ");
        return result;
    }


    private String execute(Class clz, String url, Object request, MultiValueMap headerMap, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        if (headerMap != null) {
            headers.addAll(headerMap);
        }

        for (ClientHeaderInterceptor headerInterceptor : clientHeaderInterceptorList) {
            headerInterceptor.apply(headers);
        }

        // check content type
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        StringBuilder headerStr = new StringBuilder();

        headers.entrySet().stream().forEach(
                header ->  headerStr.append(" -H ").append(header.getKey()).append(":").append(header.getValue().stream().collect(Collectors.joining()))
        );

        String json = request == null ? "" : JsonX.toJson(request);

        LoggerProxy.info(clz, "-X " + method.name() + "  " + url + headerStr + (StringUtil.isNotNull(json) ? (" -d '" + json + "'"):""));

        if (this.restTemplate == null)
            throw new NullPointerException(RestTemplate.class.getName());

        return restTemplate.exchange(url, method, new HttpEntity<>(json, headers), String.class).getBody();

    }

}
