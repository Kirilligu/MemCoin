package com.example.memcoin

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RateCheckInteractor {
    private val networkClient = NetworkClient()
    suspend fun requestRate(): String {
        return withContext(Dispatchers.IO) {
            val result = networkClient.request(MainViewModel.CRYPTO_COMPARE_URL)
            if (!result.isNullOrEmpty()) {
                parseRate(result)
            } else {
                ""
            }
        }
    }

    private fun parseRate(jsonString: String): String {
        return try {
            val jsonObject = JSONObject(jsonString)
            jsonObject.getDouble("USD").toString()
        } catch (e: Exception) {
            Log.e("RateCheckInteractor", "Ошибка парсинга", e)
            ""
        }
    }
}
