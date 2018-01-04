package com.procurement.gateway.security

import com.procurement.gateway.exception.RSAInvalidKeyException
import org.apache.commons.codec.binary.Base64
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/**
 * Service for working with RSA keys.
 */
interface RSAService {
    /**
     * Converting a string representation of a public key to an object of type [RSAPublicKey].
     *
     * @param publicKey a string representation of a public key
     *
     * @return an object of type [RSAPublicKey]
     *
     * @throws InvalidKeySpecException if the given key specification is inappropriate for this key factory
     * to produce a public key.
     */
    @Throws(InvalidKeySpecException::class)
    fun toPublicKey(publicKey: String): RSAPublicKey

    /**
     * Converting a string representation of a private key to an object of type [RSAPrivateKey].
     *
     * @param privateKey a string representation of a private key
     *
     * @return an object of type [RSAPrivateKey]
     *
     * @throws InvalidKeySpecException if the given key specification is inappropriate for this key factory
     * to produce a private key.
     */
    @Throws(InvalidKeySpecException::class)
    fun toPrivateKey(privateKey: String): RSAPrivateKey
}

/**
 * Implementation of the [RSAService] interface.
 */
class RSAServiceImpl(private val keyFactoryService: KeyFactoryService) : RSAService {

    @Throws(InvalidKeySpecException::class)
    override fun toPublicKey(publicKey: String): RSAPublicKey {
        val publicKeyBytes = decodePublicKey(publicKey)
        val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = keyFactoryService.getKeyFactory(RSA_ALGORITHM)
        return keyFactory.generatePublic(publicKeySpec) as RSAPublicKey
    }

    @Throws(InvalidKeySpecException::class)
    override fun toPrivateKey(privateKey: String): RSAPrivateKey {
        val privateKeyBytes = decodePrivateKey(privateKey)
        val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = keyFactoryService.getKeyFactory(RSA_ALGORITHM)
        return keyFactory.generatePrivate(privateKeySpec) as RSAPrivateKey
    }

    private fun decodePublicKey(key: String) = excludeBodyPublicKey(key).let { Base64.decodeBase64(it) }

    private fun decodePrivateKey(key: String) = excludeBodyPrivateKey(key).let { Base64.decodeBase64(it) }

    private fun excludeBodyPublicKey(publicKey: String): String {
        return publicKey.trim { it <= ' ' }
            .let { if (isValidPublicKeyFormat(it)) it else throw RSAInvalidKeyException(INVALID_PUBLIC_KEY_FORMAT_MSG) }
            .substring(BEGIN_PUBLIC_KEY.length, publicKey.length - END_PUBLIC_KEY.length)
            .let { formatBody(it) }
    }

    private fun excludeBodyPrivateKey(privateKey: String): String {
        return privateKey.trim { it <= ' ' }
            .let { if (isValidPrivateKeyFormat(it)) it else throw RSAInvalidKeyException(INVALID_PRIVATE_KEY_FORMAT_MSG) }
            .substring(BEGIN_PRIVATE_KEY.length, privateKey.length - END_PRIVATE_KEY.length)
            .let { formatBody(it) }
    }

    private fun isValidPublicKeyFormat(key: String): Boolean =
        key.startsWith(BEGIN_PUBLIC_KEY) && key.endsWith(END_PUBLIC_KEY)

    private fun isValidPrivateKeyFormat(key: String): Boolean =
        key.startsWith(BEGIN_PRIVATE_KEY) && key.endsWith(END_PRIVATE_KEY)

    private fun formatBody(body: String) = body.replace(NEW_LINE_PATTERN.toRegex(), "").trim { it <= ' ' }

    companion object {
        private val RSA_ALGORITHM = "RSA"
        private val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
        private val END_PUBLIC_KEY = "-----END PUBLIC KEY-----"
        private val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----"
        private val END_PRIVATE_KEY = "-----END PRIVATE KEY-----"
        private val NEW_LINE_PATTERN = "[\r\n]"
        private val INVALID_PUBLIC_KEY_FORMAT_MSG = "Invalid public key format."
        private val INVALID_PRIVATE_KEY_FORMAT_MSG = "Invalid private key format."
    }
}
