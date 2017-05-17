package org.intellift.sol.sdk.client.configuration;

import io.vavr.jackson.datatype.VavrModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * @author Achilleas Naoumidis
 */
public class RestTemplateConfiguration {

    public static void addVavrJacksonModule(final AsyncRestTemplate asyncRestTemplates) {
        asyncRestTemplates.getMessageConverters().stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new VavrModule()));
    }

    public static void addVavrJacksonModule(final RestTemplate restTemplate) {
        restTemplate.getMessageConverters().stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new VavrModule()));
    }
}
