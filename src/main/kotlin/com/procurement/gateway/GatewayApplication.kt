package com.procurement.gateway

import com.procurement.gateway.configuration.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy

@SpringBootApplication(
    scanBasePackageClasses = [ApplicationConfiguration::class]
)
@EnableZuulProxy
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
