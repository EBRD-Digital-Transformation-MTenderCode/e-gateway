package com.procurement.gateway.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * The Java-configuration of application.
 */
@Configuration
@Import(
    value = [
        GatewayConfiguration::class
    ]
)
class ApplicationConfiguration