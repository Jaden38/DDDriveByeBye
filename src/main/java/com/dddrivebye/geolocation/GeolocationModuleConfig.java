package com.dddrivebye.geolocation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Boundary marker for the geolocation bounded context.
 * Other modules MUST depend only on classes under
 * {@code com.dddrivebye.geolocation.api} — never on internal handlers,
 * domain types, or persistence adapters.
 */
@Configuration
@ComponentScan
public class GeolocationModuleConfig {
}
