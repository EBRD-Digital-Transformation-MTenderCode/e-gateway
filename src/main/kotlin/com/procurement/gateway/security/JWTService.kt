package com.procurement.gateway.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.security.interfaces.RSAPublicKey

interface JWTService {
    fun verify(token: String)
}

open class JWTServiceImpl(rsaPublicKey: RSAPublicKey) : JWTService {
    private val verifier: JWTVerifier = Algorithm.RSA256(rsaPublicKey, null)
        .let { JWT.require(it).build() }

    override fun verify(token: String) {
        verifier.verify(token)
    }
}