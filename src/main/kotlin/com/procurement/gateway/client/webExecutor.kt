package com.procurement.gateway.client

import com.procurement.gateway.exception.client.RemoteServiceException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

inline fun <reified T> WebClient.Builder.execute(uri: String, token: String, transformer: (Mono<T>) -> T): T {
    try {
        val response = this.build()
            .get()
            .uri(uri)
            .header("Authorization", token)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<T>() {})
            .doOnError(WebClientResponseException::class.java) { exception ->
                throw RemoteServiceException(
                    code = exception.statusCode,
                    payload = exception.responseBodyAsString,
                    message = "Error of remote service by uri: '$uri'.",
                    exception = exception
                )
            }
        return transformer(response)
    } catch (ex: WebClientResponseException) {
        throw RemoteServiceException(
            code = ex.statusCode,
            payload = ex.responseBodyAsString,
            message = "Error of remote service by uri: '$uri'.",
            exception = ex
        )
    }
}
