package com.procurement.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.HttpServletRequestWrapper
import com.procurement.gateway.MDCKey
import com.procurement.gateway.configuration.properties.RSAFilterProperties
import com.procurement.gateway.exception.client.RemoteServiceException
import com.procurement.gateway.mdc
import com.procurement.gateway.service.AuthClient
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class RSAFilter(
    private val RSAFilterProperties: RSAFilterProperties,
    private val authClient: AuthClient) : ZuulFilter() {

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"

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
            context.verificationAccessToken()
        } catch (ex: Exception) {
            val request = context.request as HttpServletRequestWrapper
            val uri = request.requestURI + (request.queryString?.let { "?$it" } ?: "")
            mdc(
                MDCKey.REMOTE_ADDRESS to request.remoteAddr,
                MDCKey.HTTP_METHOD to request.method,
                MDCKey.REQUEST_URI to uri) {

                when (ex) {
                    is RemoteServiceException -> {
                        context.responseStatusCode = ex.code.value()
                        context.responseBody = ex.payload
                    }
                    else -> {
                        context.responseStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                        log.warn("Error of validate token.", ex)
                    }
                }
            }
            context.setSendZuulResponse(false)
        }
        return null
    }

    fun RequestContext.verificationAccessToken() {
        val token = this.request.getHeader(AUTHORIZATION_HEADER)
        runBlocking { authClient.verification(token) }
    }
}

