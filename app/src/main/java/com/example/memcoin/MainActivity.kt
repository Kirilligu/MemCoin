package com.example.memcoin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    lateinit var textRate: TextView
    lateinit var textTargetRate: EditText
    lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()
        initView()
    }
    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.btcRate.observe(this, { rate ->
            textRate.text = "$rate USD"
        })

        viewModel.onCreate()
    }

    fun initView() {
        textRate = findViewById(R.id.textUsdRubRate)
        textTargetRate = findViewById(R.id.textTargetRate)
        rootView = findViewById(R.id.rootView)

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            viewModel.onRefreshClicked()
        }

        findViewById<Button>(R.id.btnSubscribeToRate).setOnClickListener {
            val targetRate = textTargetRate.text.toString()
            val startRate = viewModel.btcRate.value
            if (targetRate.isNotEmpty() && startRate?.isNotEmpty() == true) {
                RateCheckService.stopService(this)
                RateCheckService.startService(this, startRate, targetRate)
            } else if (targetRate.isEmpty()) {
                Snackbar.make(rootView, R.string.target_rate_empty, Snackbar.LENGTH_SHORT).show()
            } else if (startRate.isNullOrEmpty()) {
                Snackbar.make(rootView, R.string.current_rate_empty, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
