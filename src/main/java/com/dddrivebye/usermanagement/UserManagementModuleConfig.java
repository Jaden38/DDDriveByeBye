package com.dddrivebye.usermanagement;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Boundary marker for the user-management bounded context.
 * Other modules MUST depend only on classes under
 * {@code com.dddrivebye.usermanagement.api} — never on internal handlers,
 * domain types, or persistence adapters.
 */
@Configuration
@ComponentScan
public class UserManagementModuleConfig {
}
