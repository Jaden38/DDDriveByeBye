package com.dddrivebye.matching;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Boundary marker for the matching bounded context.
 * Other modules MUST depend only on classes under
 * {@code com.dddrivebye.matching.api} — never on internal handlers,
 * domain types, or persistence adapters.
 */
@Configuration
@ComponentScan
public class MatchingModuleConfig {

    @Bean
    public Clock matchingClock() {
        return Clock.systemUTC();
    }
}
