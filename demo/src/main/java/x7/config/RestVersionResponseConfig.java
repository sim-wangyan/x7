package x7.config;

import io.xream.x7.rey.api.ClientHeaderInterceptor;
import io.xream.x7.rey.api.ClientTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * @author Sim
 */
@Configuration
public class RestVersionResponseConfig {

    @Value("project.version")
    private String version;

    @Bean
    public VersionClientHeaderInterceptor versionRequestInterceptor(ClientTemplate clientTemplate){
        VersionClientHeaderInterceptor interceptor = new VersionClientHeaderInterceptor();
        clientTemplate.headerInterceptor(interceptor);
        return interceptor;
    }

    public class VersionClientHeaderInterceptor implements ClientHeaderInterceptor {

        @Override
        public void apply(HttpHeaders httpHeaders) {
            httpHeaders.add("VERSION", version);
        }
    }
}
