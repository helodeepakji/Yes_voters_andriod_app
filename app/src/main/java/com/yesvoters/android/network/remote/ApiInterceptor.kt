package com.yesvoters.android.network.remote

import android.content.Context
import android.util.Log
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.network.internet.InternetUtil
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class APIInterceptor constructor(
    private val context: Context
) : Interceptor {


    private var callingRequest: Request? = null
    private var endPoint: String? = null
    private var refreshApiCounter = false


    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            if (!InternetUtil.isInternetAvailable(context)) {
                return noInternetConnectionResponse(chain.request())
            }

            val originalRequest = chain.request()
            callingRequest = originalRequest

            val token = UserPreferences.getUserToken()

            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Accept", "application/json")

            if (token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val newRequest = requestBuilder.build()

            logRequest(newRequest)

            val response = chain.proceed(newRequest)

            return logResponse(newRequest, response)

        } catch (e: Exception) {
            e.printStackTrace()
            showErrorMessage(e)
            return createErrorResponse(500, "Internal Server Error", chain.request())
        }
    }

    private fun logRequest(request: Request) {
        try {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            val requestBody = buffer.readUtf8()
            endPoint = request.url.toString().split("/").lastOrNull()
            Log.d(TAG, "Request: ${request.method} ${request.url}\nBody: $requestBody\nHeaders: ${request.headers}")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging request: ${e.localizedMessage}")
        }
    }

    private fun logResponse(request: Request, response: Response): Response {
        return try {
            val responseBody = response.peekBody(1_000_000) // Limit to 1MB
            val responseString = responseBody.string()
            Log.d(TAG, "Response: ${response.code} ${request.url}\n$responseString")

            if (response.code == 403 && !refreshApiCounter) {
                refreshApiCounter = true
                callingRequest = request
                Log.w(TAG, "403 Forbidden – Token might need refresh.")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "Error logging response: ${e.localizedMessage}")
            response
        }
    }

    private fun showErrorMessage(e: Exception) {
        e.printStackTrace()
    }

    private fun createErrorResponse(code: Int, message: String, request: Request): Response {
        return Response.Builder()
            .request(request)
            .code(code)
            .protocol(Protocol.HTTP_1_1)
            .message(message)
            .body("".toResponseBody(APPLICATION_JSON))
            .build()
    }

    private fun noInternetConnectionResponse(request: Request): Response {
        return try {
            val jsonResponse = JSONObject().apply {
                put("success", false)
                put("statusCode", 404)
                put("message", context.getString(R.string.please_check_your_internet_connection_try_again))
                put("result", JSONObject())
                put("time", System.currentTimeMillis())
            }

            val responseBody = jsonResponse.toString().toResponseBody(APPLICATION_JSON)

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message(context.getString(R.string.please_check_your_internet_connection_try_again))
                .body(responseBody)
                .build()
        } catch (e: JSONException) {
            e.printStackTrace()
            throw IOException("Error creating dummy response", e)
        }
    }

    companion object {
        private const val TAG = "APIInterceptor"
        val APPLICATION_JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
    }
}
