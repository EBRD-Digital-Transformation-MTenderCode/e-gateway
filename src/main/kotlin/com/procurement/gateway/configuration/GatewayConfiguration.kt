package com.procurement.gateway.configuration

import com.procurement.gateway.configuration.properties.RSAFilterProperties
import com.procurement.gateway.filter.RSAFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    value = [
        SecurityConfiguration::class
    ]
)
@EnableConfigurationProperties(
    value = [
        RSAFilterProperties::class
    ]
)
class GatewayConfiguration(private val RSAFilterProperties: RSAFilterProperties,
                           private val securityConfiguration: SecurityConfiguration) {
    @Bean
    fun rsaFilter() = RSAFilter(RSAFilterProperties, securityConfiguration.jwtService())
}