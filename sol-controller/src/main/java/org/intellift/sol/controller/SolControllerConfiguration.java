package org.intellift.sol.controller;

import javaslang.jackson.datatype.JavaslangModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolControllerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JavaslangModule javaslangModule() {
        return new JavaslangModule();
    }
}
