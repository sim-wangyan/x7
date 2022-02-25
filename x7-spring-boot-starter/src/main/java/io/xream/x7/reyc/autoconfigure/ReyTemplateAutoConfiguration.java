package io.xream.x7.reyc.autoconfigure;

import io.xream.x7.reyc.api.ReyTemplate;
import io.xream.x7.reyc.internal.R4JTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @Author Sim
 */
public class ReyTemplateAutoConfiguration {

    @ConditionalOnMissingBean(ReyTemplate.class)
    @Bean
    public ReyTemplate reyTemplate() {
        return new R4JTemplate();
    }
}
