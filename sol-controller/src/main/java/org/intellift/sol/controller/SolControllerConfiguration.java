package org.intellift.sol.controller;

import io.vavr.jackson.datatype.VavrModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Achilleas Naoumidis
 */
@Configuration
public class SolControllerConfiguration {

    @Bean
    @ConditionalOnMissingBean(VavrModule.class)
    public VavrModule vavrModule() {
        return new VavrModule();
    }
}
