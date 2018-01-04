package com.procurement.gateway.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class RSAKeyProperties(
    var publicKey: String = ""
)