package com.procurement.gateway.configuration

import com.procurement.gateway.configuration.properties.RSAFilterProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ComponentScan(basePackages = ["com.procurement.gateway.filter", "com.procurement.gateway.service"])
@EnableConfigurationProperties(value = [RSAFilterProperties::class])
class GatewayConfiguration {
    @Bean
    fun loadBalancedWebClientBuilder(): WebClient.Builder = WebClient.builder()
}