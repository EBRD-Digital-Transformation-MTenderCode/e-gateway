package com.procurement.gateway.security

import com.procurement.gateway.exception.RSAInvalidKeyException
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RSAServiceTest {
    @Test
    fun toPublicKey() {
        val generator = RSAKeyGenerator()
        val keyPair = generator.generate(NUM_BITS)

        val rsaPublicKey = rsaService.toPublicKey(keyPair.publicKey)

        val publicKey = BEGIN_PUBLIC_KEY + NEW_LINE_PATTERN +
            Base64.encodeBase64String(rsaPublicKey.encoded) +
            NEW_LINE_PATTERN + END_PUBLIC_KEY

        assertEquals(keyPair.publicKey, publicKey)
    }

    @Test
    fun invalidPublicKey() {
        val generator = RSAKeyGenerator()
        val keyPair = generator.generate(NUM_BITS)
        val publicKey = keyPair.publicKey

        assertEquals(
            INVALID_PUBLIC_KEY_FORMAT_MSG,
            assertThrows(
                RSAInvalidKeyException::class.java,
                { rsaService.toPublicKey(publicKey.substring(2)) }
            ).message
        )

        assertEquals(
            INVALID_PUBLIC_KEY_FORMAT_MSG,
            assertThrows(
                RSAInvalidKeyException::class.java,
                { rsaService.toPublicKey(publicKey.substring(0, publicKey.length - 2)) }
            ).message
        )
    }

    @Test
    fun toPrivateKey() {
        val generator = RSAKeyGenerator()
        val keyPair = generator.generate(NUM_BITS)

        val rsaPrivateKey = rsaService.toPrivateKey(keyPair.privateKey)

        val privateKey = BEGIN_PRIVATE_KEY + NEW_LINE_PATTERN +
            Base64.encodeBase64String(rsaPrivateKey.encoded) +
            NEW_LINE_PATTERN + END_PRIVATE_KEY

        assertEquals(keyPair.privateKey, privateKey)
    }

    @Test
    fun invalidPrivateKey() {
        val generator = RSAKeyGenerator()
        val keyPair = generator.generate(NUM_BITS)
        val privateKey = keyPair.privateKey

        assertEquals(
            INVALID_PRIVATE_KEY_FORMAT_MSG,
            assertThrows(
                RSAInvalidKeyException::class.java,
                { rsaService.toPrivateKey(privateKey.substring(2)) }
            ).message
        )

        assertEquals(
            INVALID_PRIVATE_KEY_FORMAT_MSG,
            assertThrows(
                RSAInvalidKeyException::class.java,
                { rsaService.toPrivateKey(privateKey.substring(0, privateKey.length - 2)) }
            ).message
        )
    }

    companion object {
        private val NUM_BITS = 2048
        private val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
        private val END_PUBLIC_KEY = "-----END PUBLIC KEY-----"
        private val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----"
        private val END_PRIVATE_KEY = "-----END PRIVATE KEY-----"
        private val INVALID_PUBLIC_KEY_FORMAT_MSG = "Invalid public key format."
        private val NEW_LINE_PATTERN = "\n"
        private val INVALID_PRIVATE_KEY_FORMAT_MSG = "Invalid private key format."

        private var rsaService: RSAServiceImpl = RSAServiceImpl(KeyFactoryServiceImpl())
    }
}