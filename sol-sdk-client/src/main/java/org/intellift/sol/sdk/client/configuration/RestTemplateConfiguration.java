package org.intellift.sol.sdk.client.configuration;

import javaslang.jackson.datatype.JavaslangModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * @author Achilleas Naoumidis
 */
public class RestTemplateConfiguration {

    public static void addJavaslangJacksonModule(final AsyncRestTemplate asyncRestTemplates) {
        asyncRestTemplates.getMessageConverters().stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new JavaslangModule()));
    }

    public static void addJavaslangJacksonModule(final RestTemplate restTemplate) {
        restTemplate.getMessageConverters().stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new JavaslangModule()));
    }
}
