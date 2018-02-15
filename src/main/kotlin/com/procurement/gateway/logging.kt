package com.procurement.gateway

import org.slf4j.MDC

enum class MDCKey(val keyName: String) {
    REMOTE_ADDRESS("remoteAddr"),
    HTTP_METHOD("httpMethod"),
    REQUEST_URI("uri");

    infix fun to(value: String): MDCProp {
        return MDCProp(this.keyName, value)
    }
}

data class MDCProp(val key: String, val value: String)

fun mdc(key: MDCKey, value: String) {
    MDC.put(key.keyName, value)
}

fun mdc(vararg props: MDCProp) {
    for (prop in props) MDC.put(prop.key, prop.value)
}

inline fun <T> mdc(vararg props: MDCProp, block: () -> T): T {
    for (prop in props) MDC.put(prop.key, prop.value)
    try {
        return block()
    } finally {
        for (prop in props) MDC.remove(prop.key)
    }
}

fun mdc(key: MDCKey) {
    MDC.remove(key.keyName)
}

fun mdc(vararg keys: MDCKey) {
    for (key in keys) MDC.remove(key.keyName)
}