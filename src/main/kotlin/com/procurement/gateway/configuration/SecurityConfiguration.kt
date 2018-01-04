package com.procurement.gateway.configuration

import com.procurement.gateway.configuration.properties.RSAKeyProperties
import com.procurement.gateway.security.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        RSAKeyProperties::class
    ]
)
class SecurityConfiguration(private val rsaKeyProperties: RSAKeyProperties) {
    @Bean
    fun keyFactoryService(): KeyFactoryService = KeyFactoryServiceImpl()

    @Bean
    fun rsaService(): RSAService = RSAServiceImpl(keyFactoryService())

    @Bean
    fun jwtService(): JWTService = JWTServiceImpl(rsaService().toPublicKey(rsaKeyProperties.publicKey))
}