package com.example.memcoin

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class NetworkClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    fun request(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: Exception) {
            Log.e("NetworkClient", "Ошибка сети", e)
            null
        }
    }
}
