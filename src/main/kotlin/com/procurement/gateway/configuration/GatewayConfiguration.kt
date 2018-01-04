package com.procurement.gateway.configuration

import com.procurement.gateway.configuration.properties.ProxyProperties
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
        ProxyProperties::class
    ]
)
class GatewayConfiguration(private val proxyProperties: ProxyProperties,
                           private val securityConfiguration: SecurityConfiguration) {
    @Bean
    fun rsaFilter() = RSAFilter(proxyProperties, securityConfiguration.jwtService())
}