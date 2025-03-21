package com.example.memcoin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

class RateCheckService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var rateCheckAttempt = 0
    private lateinit var startRate: BigDecimal
    private lateinit var targetRate: BigDecimal
    private val rateCheckInteractor = RateCheckInteractor()

    private val rateCheckRunnable: Runnable = Runnable {
        requestAndCheckRate()
    }

    private fun requestAndCheckRate() {
        GlobalScope.launch(Dispatchers.IO) {
            val result = rateCheckInteractor.requestRate()
            if (result.isNotEmpty()) {
                val currentRate = BigDecimal(result)
                if (currentRate >= targetRate || currentRate <= startRate) {
                    val isIncrease = currentRate >= targetRate
                    sendNotification(currentRate, isIncrease)
                    stopSelf()
                }
            }
            rateCheckAttempt++
            if (rateCheckAttempt < RATE_CHECK_ATTEMPTS_MAX) {
                handler.postDelayed(rateCheckRunnable, RATE_CHECK_INTERVAL)
            } else {
                stopSelf()
            }
        }
    }

    private fun sendNotification(rate: BigDecimal, isIncrease: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Rate Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val arrow = if (isIncrease) "🔼" else "🔽"
        val message = "Курс изменился: $rate USD $arrow"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Изменение курса BTC")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRate = BigDecimal(intent?.getStringExtra(ARG_START_RATE))
        targetRate = BigDecimal(intent?.getStringExtra(ARG_TARGET_RATE))
        Log.d(TAG, "onStartCommand startRate = $startRate targetRate = $targetRate")
        handler.post(rateCheckRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rateCheckRunnable)
    }

    companion object {
        const val TAG = "RateCheckService"
        const val CHANNEL_ID = "RateCheckChannel"
        const val NOTIFICATION_ID = 1
        const val RATE_CHECK_INTERVAL = 5000L
        const val RATE_CHECK_ATTEMPTS_MAX = 100
        const val ARG_START_RATE = "ARG_START_RATE"
        const val ARG_TARGET_RATE = "ARG_TARGET_RATE"

        fun startService(context: Context, startRate: String, targetRate: String) {
            context.startService(Intent(context, RateCheckService::class.java).apply {
                putExtra(ARG_START_RATE, startRate)
                putExtra(ARG_TARGET_RATE, targetRate)
            })
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, RateCheckService::class.java))
        }
    }
}
