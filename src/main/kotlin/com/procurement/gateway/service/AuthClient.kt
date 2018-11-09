package com.procurement.gateway.service

import com.procurement.gateway.client.execute
import kotlinx.coroutines.experimental.reactive.awaitFirst
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder


interface AuthClient {
    suspend fun verification(token: String?)
}

@Service
class AuthClientImpl(private val webClientBuilder: WebClient.Builder) : AuthClient {
    companion object {
        val AUTH_URI: String = UriComponentsBuilder.fromHttpUrl("http://auth:8080")
            .pathSegment("auth")
            .pathSegment("verification")
            .toUriString()
    }

    override suspend fun verification(token: String?) {
        webClientBuilder.execute<String>(AUTH_URI, token ?: "") { it.awaitFirst() }
    }
}