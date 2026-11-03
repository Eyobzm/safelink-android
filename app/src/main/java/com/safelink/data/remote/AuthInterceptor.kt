package com.safelink.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** NFR-SEC-03: app token. Not an API key for any third party. */
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $token").build())
}
