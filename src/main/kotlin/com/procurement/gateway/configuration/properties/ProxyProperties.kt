package com.procurement.gateway.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zuul.filters")
data class ProxyProperties(
    var exclude: MutableSet<String> = HashSet()
)