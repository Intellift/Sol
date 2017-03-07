package org.intellift.sol.sdk.client;

import javaslang.jackson.datatype.JavaslangModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Achilleas Naoumidis
 */
@Configuration
public class SolSdkClientConfiguration {

    @Bean
    @ConditionalOnMissingBean(JavaslangModule.class)
    public JavaslangModule javaslangModule() {
        return new JavaslangModule();
    }

    @Autowired
    @ConditionalOnBean(RestTemplate.class)
    public void addJavaslangJacksonModuleToRestTemplates(final List<RestTemplate> restTemplates, final JavaslangModule javaslangModule) {
        restTemplates.forEach(restTemplate -> restTemplate.getMessageConverters()
                .forEach(converter -> {
                    if (converter instanceof MappingJackson2HttpMessageConverter) {
                        ((MappingJackson2HttpMessageConverter) converter).getObjectMapper().registerModule(javaslangModule);
                    }
                }));
    }

    @Autowired
    @ConditionalOnBean(AsyncRestTemplate.class)
    public void addJavaslangJacksonModuleToAsyncRestTemplates(final List<AsyncRestTemplate> asyncRestTemplates, final JavaslangModule javaslangModule) {
        asyncRestTemplates.forEach(asyncRestTemplate -> asyncRestTemplate.getMessageConverters()
                .forEach(converter -> {
                    if (converter instanceof MappingJackson2HttpMessageConverter) {
                        ((MappingJackson2HttpMessageConverter) converter).getObjectMapper().registerModule(javaslangModule);
                    }
                }));
    }
}
