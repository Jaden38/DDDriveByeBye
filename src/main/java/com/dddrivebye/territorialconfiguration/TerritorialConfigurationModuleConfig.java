package com.dddrivebye.territorialconfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Boundary marker for the territorial-configuration bounded context.
 * Other modules MUST depend only on classes under
 * {@code com.dddrivebye.territorialconfiguration.api} — never on internal
 * handlers, domain types, or persistence adapters.
 */
@Configuration
@ComponentScan
public class TerritorialConfigurationModuleConfig {
}
