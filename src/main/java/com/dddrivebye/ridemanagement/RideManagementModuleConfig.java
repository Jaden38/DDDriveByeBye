package com.dddrivebye.ridemanagement;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Ride Management module.
 * Other modules MUST depend only on classes under com.dddrivebye.ridemanagement.api.
 */
@Configuration
@ComponentScan
public class RideManagementModuleConfig {
}
