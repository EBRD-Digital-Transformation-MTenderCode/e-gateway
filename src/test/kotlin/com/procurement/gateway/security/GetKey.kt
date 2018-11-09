package com.procurement.gateway.security

fun main(args: Array<String>) {
    val rsaKeyPair = RSAKeyGenerator().generate(2048)
    println(rsaKeyPair.publicKey)
    println(rsaKeyPair.privateKey)
}