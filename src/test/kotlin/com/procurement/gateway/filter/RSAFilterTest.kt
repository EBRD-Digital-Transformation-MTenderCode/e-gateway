package com.procurement.gateway.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.HttpServletRequestWrapper
import com.procurement.gateway.configuration.properties.RSAFilterProperties
import com.procurement.gateway.exception.InvalidAuthorizationHeaderTypeException
import com.procurement.gateway.exception.NoSuchAuthorizationHeaderException
import com.procurement.gateway.security.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Matchers.anyString
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class RSAFilterTest {
    companion object {
        val RSA_KEY_PAIR = RSAKeyGenerator().generate(2048)
        const val AUTHORIZATION_HEADER = "Authorization"
        const val WWW_AUTHENTICATE = "WWW-Authenticate"
        const val REALM = "Bearer realm=\"yoda\""
        const val EXPIRED_TOKEN_MSG = "$REALM, error_code=\"invalid_token\", error_message=\"The access token expired.\""
    }

    private val proxyProperties = RSAFilterProperties()
    private val rsaService: RSAService = RSAServiceImpl(KeyFactoryServiceImpl())
    private val rsaPublicKey = rsaService.toPublicKey(RSA_KEY_PAIR.publicKey)
    private val rsaPrivateKey = rsaService.toPrivateKey(RSA_KEY_PAIR.privateKey)
    private val jwtService = JWTServiceImpl(rsaPublicKey)
    private val rsaFilter = RSAFilter(proxyProperties, jwtService)

    @Test
    fun filterType() {
        assertEquals(rsaFilter.filterType(), PRE_TYPE)
    }

    @Test
    fun filterOrder() {
        assertEquals(rsaFilter.filterOrder(), FilterConstants.PRE_DECORATION_FILTER_ORDER - 1)
    }

    @Test
    fun shouldFilterFalse() {
        initContext(MockHttpServletRequest())
        proxyProperties.exclude.add("EndPoint")

        assertFalse(rsaFilter.shouldFilter())

        val ctx = RequestContext.getCurrentContext()
        ctx.set("proxy", "EndPoint")
        assertFalse(rsaFilter.shouldFilter())
    }

    @Test
    fun shouldFilterTrue() {
        initContext(MockHttpServletRequest())

        val ctx = RequestContext.getCurrentContext()

        ctx.set("proxy", "EndPoint")
        assertTrue(rsaFilter.shouldFilter())
    }

    @Test
    fun noSuchAuthorizationHeaderException() {
        initContext(MockHttpServletRequest())
        val ctx = RequestContext.getCurrentContext()

        assertThrows(NoSuchAuthorizationHeaderException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()
        val response = ctx.response
        assertEquals(response.status, HttpStatus.UNAUTHORIZED.value())
        assertEquals(response.getHeader(WWW_AUTHENTICATE), REALM)
    }

    @Test
    fun invalidAuthorizationHeaderTypeException() {
        val request = MockHttpServletRequest()
        request.addHeader(AUTHORIZATION_HEADER, "Basic rCwlvdbH6dvEOfou0/gCFQsHUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a0")
        initContext(request)
        val ctx = RequestContext.getCurrentContext()

        assertThrows(InvalidAuthorizationHeaderTypeException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()

        val response = ctx.response
        assertEquals(response.status, HttpStatus.UNAUTHORIZED.value())
        assertEquals(response.getHeader(WWW_AUTHENTICATE), REALM)
    }

    @Test
    fun tokenExpiredException() {
        val request = MockHttpServletRequest()
        request.addHeader(AUTHORIZATION_HEADER, "Bearer ${getToken(LocalDateTime.now().minusSeconds(1))}")
        initContext(request)

        val ctx = RequestContext.getCurrentContext()

        assertThrows(TokenExpiredException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()

        val response = ctx.response
        assertEquals(response.status, HttpStatus.UNAUTHORIZED.value())
        assertEquals(response.getHeader(WWW_AUTHENTICATE), EXPIRED_TOKEN_MSG)
    }

    @Test
    fun signatureVerificationException() {
        val token = getToken(LocalDateTime.now()).let { it.substring(0, it.length - 2) }
        val request = MockHttpServletRequest()
        request.addHeader(AUTHORIZATION_HEADER, "Bearer $token")
        initContext(request)

        val ctx = RequestContext.getCurrentContext()

        assertThrows(SignatureVerificationException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()

        val response = ctx.response
        assertEquals(response.status, HttpStatus.UNAUTHORIZED.value())
        assertEquals(response.getHeader(WWW_AUTHENTICATE), RSAFilter.REALM)
    }

    @Test
    fun jwtVerificationException() {
        val jwtService = mock(JWTServiceImpl::class.java)
        doThrow(JWTVerificationException::class.java).`when`(jwtService).verify(anyString())
        val rsaFilter = RSAFilter(proxyProperties, jwtService)

        val token = getToken(LocalDateTime.now())
        val request = MockHttpServletRequest()
        request.addHeader(AUTHORIZATION_HEADER, "Bearer $token")
        initContext(request)

        val ctx = RequestContext.getCurrentContext()

        assertThrows(JWTVerificationException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()

        val response = ctx.response
        assertEquals(response.status, HttpStatus.UNAUTHORIZED.value())
        assertEquals(response.getHeader(WWW_AUTHENTICATE), RSAFilter.REALM)
    }

    @Test
    fun runtimeException() {
        val jwtService = mock(JWTServiceImpl::class.java)
        doThrow(RuntimeException::class.java).`when`(jwtService).verify(anyString())

        val rsaFilter = RSAFilter(proxyProperties, jwtService)

        val token = getToken(LocalDateTime.now())
        val request = MockHttpServletRequest()
        request.addHeader(AUTHORIZATION_HEADER, "Bearer $token")
        initContext(request)

        val ctx = RequestContext.getCurrentContext()

        assertThrows(RuntimeException::class.java) {
            rsaFilter.validateToken(ctx)
        }

        rsaFilter.run()

        val response = ctx.response
        assertEquals(response.status, HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    @AfterEach
    fun reset() {
        RequestContext.testSetCurrentContext(null)
        proxyProperties.exclude.clear()
    }

    private fun initContext(request: MockHttpServletRequest) {
        val context = RequestContext()
        context.request = HttpServletRequestWrapper(request)
        context.response = MockHttpServletResponse()
        RequestContext.testSetCurrentContext(context)
    }

    private fun getToken(dateTime: LocalDateTime): String {
        val algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey)

        val date = Date.from(
            ZonedDateTime.of(dateTime, ZoneId.systemDefault()).toInstant()
        )

        return JWT.create()
            .withExpiresAt(date)
            .sign(algorithm)
    }
}