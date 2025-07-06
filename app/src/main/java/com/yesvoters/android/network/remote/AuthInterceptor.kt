package com.yesvoters.android.network.remote

import okhttp3.Interceptor
import okhttp3.Response
import com.yesvoters.android.database.UserPreferences

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = UserPreferences.getUserToken() // Get your token here

        val newRequest = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
