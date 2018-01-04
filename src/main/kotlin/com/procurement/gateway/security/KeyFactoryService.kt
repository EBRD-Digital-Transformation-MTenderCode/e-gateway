package com.procurement.gateway.security

import com.procurement.gateway.exception.NotSupportAlgorithmException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException

/**
 * Service for obtaining KeyFactory.
 */
interface KeyFactoryService {
    /**
     * Returns an object of type KeyFactory.
     *
     * @param algorithm the name of the requested key algorithm
     *
     * @return an object of type [KeyFactory]
     *
     * @throws NotSupportAlgorithmException if [KeyFactory] not support specified algorithm.
     */
    fun getKeyFactory(algorithm: String): KeyFactory
}

/**
 * Implementation of the [KeyFactoryService] interface.
 */
class KeyFactoryServiceImpl : KeyFactoryService {
    override fun getKeyFactory(algorithm: String): KeyFactory = try {
        KeyFactory.getInstance(algorithm)
    } catch (e: NoSuchAlgorithmException) {
        throw NotSupportAlgorithmException("KeyFactory not support specified algorithm: " + algorithm, e)
    }
}