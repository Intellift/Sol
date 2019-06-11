package org.intellift.sol.controller;

import io.vavr.jackson.datatype.VavrModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolControllerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VavrModule vavrModule() {
        return new VavrModule();
    }
}
