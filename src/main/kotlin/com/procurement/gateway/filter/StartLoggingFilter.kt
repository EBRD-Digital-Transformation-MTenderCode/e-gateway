package com.procurement.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.HttpServletRequestWrapper
import com.procurement.gateway.MDCKey
import com.procurement.gateway.mdc
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE
import org.springframework.stereotype.Component

@Component
class StartLoggingFilter : ZuulFilter() {
    override fun filterType(): String = PRE_TYPE

    override fun filterOrder(): Int = PRE_DECORATION_FILTER_ORDER - 1

    override fun shouldFilter(): Boolean = true

    override fun run(): Any? {
        val context = RequestContext.getCurrentContext()
        val request = context.request as HttpServletRequestWrapper
        val uri = request.requestURI + (request.queryString?.let { "?$it" } ?: "")

        mdc(MDCKey.REMOTE_ADDRESS, request.remoteAddr)
        mdc(MDCKey.HTTP_METHOD, request.method)
        mdc(MDCKey.REQUEST_URI, uri)

        return null
    }
}

