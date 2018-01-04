package com.procurement.gateway.security

import com.procurement.gateway.model.rsa.RSAKeyPair
import org.apache.commons.codec.binary.Base64
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class RSAKeyGenerator {

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun generate(numBits: Int): RSAKeyPair {
        val keyPair = genKeyPair(numBits)
        val privateKey = keyPair.private
        val publicKey = keyPair.public
        return RSAKeyPair(
            BEGIN_PRIVATE_KEY + NEW_LINE_PATTERN +
                Base64.encodeBase64String(privateKey.encoded) +
                NEW_LINE_PATTERN + END_PRIVATE_KEY,
            BEGIN_PUBLIC_KEY + NEW_LINE_PATTERN +
                Base64.encodeBase64String(publicKey.encoded) +
                NEW_LINE_PATTERN + END_PUBLIC_KEY
        )
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun genKeyPair(numBits: Int): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(ALGORITHM)
        keyGen.initialize(numBits)
        val keyPair = keyGen.genKeyPair()
        validateKeyPair(keyPair)
        return keyPair
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun validateKeyPair(keyPair: KeyPair) {
        val privateKey = keyPair.private
        val publicKey = keyPair.public

        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        val privateKeySpec = PKCS8EncodedKeySpec(privateKey.encoded)
        val newPrivateKey = keyFactory.generatePrivate(privateKeySpec)

        val publicKeySpec = X509EncodedKeySpec(publicKey.encoded)
        val newPublicKey = keyFactory.generatePublic(publicKeySpec)

        if (isNotEqualsPrivateKeys(privateKey, newPrivateKey) || isNotEqualsPublicKeys(publicKey, newPublicKey)) {
            throw RuntimeException("Validation RSA keys failed.")
        }
    }

    private fun isNotEqualsPrivateKeys(privateKey1: PrivateKey, privateKey2: PrivateKey): Boolean {
        return privateKey1 != privateKey2
    }

    private fun isNotEqualsPublicKeys(publicKey1: PublicKey, publicKey2: PublicKey): Boolean {
        return publicKey1 != publicKey2
    }

    companion object {
        private val ALGORITHM = "RSA"
        private val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
        private val END_PUBLIC_KEY = "-----END PUBLIC KEY-----"
        private val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----"
        private val END_PRIVATE_KEY = "-----END PRIVATE KEY-----"
        private val NEW_LINE_PATTERN = "\n"
    }
}