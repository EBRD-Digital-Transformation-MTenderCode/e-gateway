package com.procurement.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.procurement.gateway.MDCKey
import com.procurement.gateway.mdc
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER
import org.springframework.stereotype.Component

@Component
class EndLoggingFilter : ZuulFilter() {
    override fun filterType(): String = POST_TYPE

    override fun filterOrder(): Int = SEND_RESPONSE_FILTER_ORDER - 1

    override fun shouldFilter(): Boolean = true

    override fun run(): Any? {
        mdc(MDCKey.REQUEST_URI)
        mdc(MDCKey.HTTP_METHOD)
        mdc(MDCKey.REMOTE_ADDRESS)

        return null
    }
}

