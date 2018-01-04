package com.procurement.gateway.exception

/**
 * NotSupportAlgorithmException is exception is thrown when a particular cryptographic algorithm is requested
 * but is not available in the environment.
 */
class NotSupportAlgorithmException(message: String, cause: Throwable) : RuntimeException(message, cause)