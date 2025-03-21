package com.example.memcoin

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val btcRate = MutableLiveData<String>()
    val rateCheckInteractor = RateCheckInteractor()

    fun onCreate() {
        refreshRate()
    }
    fun onRefreshClicked() {
        refreshRate()
    }

    private fun refreshRate() {
        GlobalScope.launch(Dispatchers.Main) {
            val rate = rateCheckInteractor.requestRate()
            Log.d(TAG, "btcRate = $rate")
            btcRate.value = rate
        }
    }
    companion object {
        const val TAG = "MainViewModel"
        const val CRYPTO_COMPARE_URL = "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD"
    }
}
