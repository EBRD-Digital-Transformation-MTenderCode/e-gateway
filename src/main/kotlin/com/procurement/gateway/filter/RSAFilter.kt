package com.procurement.gateway.filter

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.HttpServletRequestWrapper
import com.procurement.gateway.MDCKey
import com.procurement.gateway.configuration.properties.RSAFilterProperties
import com.procurement.gateway.exception.InvalidAuthorizationHeaderTypeException
import com.procurement.gateway.exception.NoSuchAuthorizationHeaderException
import com.procurement.gateway.mdc
import com.procurement.gateway.security.JWTService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE
import org.springframework.http.HttpStatus

class RSAFilter(private val RSAFilterProperties: RSAFilterProperties, private val jwtService: JWTService) : ZuulFilter() {
    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val AUTHORIZATION_PREFIX_BEARER = "Bearer "
        const val WWW_AUTHENTICATE = "WWW-Authenticate"
        const val REALM = "Bearer realm=\"yoda\""

        val log: Logger = LoggerFactory.getLogger(RSAFilter::class.java)
    }

    override fun filterType(): String = PRE_TYPE

    override fun filterOrder(): Int = PRE_DECORATION_FILTER_ORDER + 1

    override fun shouldFilter(): Boolean {
        val context = RequestContext.getCurrentContext()
        val proxy = context["proxy"]
        return !(proxy == null || RSAFilterProperties.exclude.contains(proxy))
    }

    override fun run(): Any? {
        val context = RequestContext.getCurrentContext()
        try {
            validateToken(context)
        } catch (ex: Exception) {
            val request = context.request as HttpServletRequestWrapper
            val uri = request.requestURI + (request.queryString?.let { "?" + it } ?: "")
            mdc(MDCKey.REMOTE_ADDRESS to request.remoteAddr,
                MDCKey.HTTP_METHOD to request.method,
                MDCKey.REQUEST_URI to uri
            ) {
                when (ex) {
                    is NoSuchAuthorizationHeaderException -> {
                        context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                        context.response.addHeader(WWW_AUTHENTICATE, REALM)
                        log.warn("No access token.")
                    }
                    is InvalidAuthorizationHeaderTypeException -> {
                        context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                        context.response.addHeader(WWW_AUTHENTICATE, REALM)
                        log.warn("Invalid type of token.")
                    }
                    is TokenExpiredException -> {
                        context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                        context.response.addHeader(
                            WWW_AUTHENTICATE,
                            "$REALM, error_code=\"invalid_token\", error_message=\"The access token expired.\""
                        )
                        log.warn("The access token expired.")
                    }
                    is SignatureVerificationException -> {
                        context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                        context.response.addHeader(WWW_AUTHENTICATE, REALM)
                        log.warn("Invalid signature of a token.")
                    }
                    is JWTVerificationException -> {
                        context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                        context.response.addHeader(WWW_AUTHENTICATE, REALM)
                        log.warn("Error of verify token.")
                    }
                    else -> {
                        context.responseStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                        log.warn("Error of validate token.")
                    }
                }
            }
            context.setSendZuulResponse(false)
        }
        return null
    }

    fun validateToken(context: RequestContext) {
        val token = context.getToken()
        jwtService.verify(token)
    }

    private fun RequestContext.getToken(): String {
        getAuthorizationHeader()?.let { return getAuthorizationToken(it) }
        throw NoSuchAuthorizationHeaderException()
    }

    private fun getAuthorizationToken(header: String): String =
        if (header.startsWith(AUTHORIZATION_PREFIX_BEARER))
            header.substring(AUTHORIZATION_PREFIX_BEARER.length)
        else
            throw InvalidAuthorizationHeaderTypeException()

    private fun RequestContext.getAuthorizationHeader(): String? = this.request.getHeader(AUTHORIZATION_HEADER)
}

