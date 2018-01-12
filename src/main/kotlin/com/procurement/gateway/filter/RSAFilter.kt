package com.procurement.gateway.filter

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.HttpServletRequestWrapper
import com.procurement.gateway.configuration.properties.RSAFilterProperties
import com.procurement.gateway.exception.InvalidAuthorizationHeaderTypeException
import com.procurement.gateway.exception.NoSuchAuthorizationHeaderException
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
            when (ex) {
                is NoSuchAuthorizationHeaderException -> {
                    context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                    context.response.addHeader(WWW_AUTHENTICATE, REALM)
                    log.debug("No access token.", ex)
                }
                is InvalidAuthorizationHeaderTypeException -> {
                    context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                    context.response.addHeader(WWW_AUTHENTICATE, REALM)
                    log.debug("Invalid type of token.", ex)
                }
                is TokenExpiredException -> {
                    context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                    context.response.addHeader(
                        WWW_AUTHENTICATE,
                        "$REALM, error_code=\"invalid_token\", error_message=\"The access token expired.\""
                    )
                    log.debug("The access token expired.", ex)
                }
                is SignatureVerificationException -> {
                    context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                    context.response.addHeader(WWW_AUTHENTICATE, REALM)
                    log.error("Invalid signature of a tokin.\n${request.getRequestInfo()}", ex)
                }
                is JWTVerificationException -> {
                    context.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
                    context.response.addHeader(WWW_AUTHENTICATE, REALM)
                    log.error("Error of verify token.\n${request.getRequestInfo()}", ex)
                }
                else -> {
                    context.responseStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                    log.error("Error of validate token.\n${request.getRequestInfo()}", ex)
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

    private fun HttpServletRequestWrapper.getRequestInfo(): String {
        val contentType = this.contentType?.let { "\nContent-Type: $it" } ?: ""
        val content = this.contentData?.let { "\n" + String(it) } ?: ""
        return "Remote address: ${this.remoteAddr}\n" +
            "${this.protocol}\n" +
            "${this.method} ${this.requestURI}?${this.queryString}" +
            contentType +
            content
    }
}

