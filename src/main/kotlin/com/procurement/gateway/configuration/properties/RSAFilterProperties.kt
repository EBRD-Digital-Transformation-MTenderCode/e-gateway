package com.procurement.gateway.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zuul.rsa-filter")
data class RSAFilterProperties(
    var exclude: MutableSet<String> = mutableSetOf()
)