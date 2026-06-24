package com.turkcell.lyraapp.data.network

import com.turkcell.lyraapp.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // get token blocking
        val token = tokenStore.accessTokenBlocking()

        val requestBuilder = originalRequest.newBuilder()

        // Only add Bearer if we have a token
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
