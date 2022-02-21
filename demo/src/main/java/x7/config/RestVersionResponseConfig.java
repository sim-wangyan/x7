package x7.config;

import io.xream.x7.base.KV;
import io.xream.x7.reyc.api.ClientHeaderInterceptor;
import io.xream.x7.reyc.internal.RestTemplateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Sim
 */
@Configuration
public class RestVersionResponseConfig {

    @Value("project.version")
    private String version;

    @Bean
    public VersionClientHeaderInterceptor versionRequestInterceptor(RestTemplateWrapper restTemplateWrapper){
        VersionClientHeaderInterceptor interceptor = new VersionClientHeaderInterceptor();
        restTemplateWrapper.headerInterceptor(interceptor);
        return interceptor;
    }

    public class VersionClientHeaderInterceptor implements ClientHeaderInterceptor {

        @Override
        public void apply(HttpHeaders httpHeaders) {
            httpHeaders.add("VERSION", version);
        }
    }
}
