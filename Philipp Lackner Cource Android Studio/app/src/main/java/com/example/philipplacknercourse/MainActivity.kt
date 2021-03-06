package com.example.philipplacknercourse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartService.setOnClickListener {
            Intent(this, MyService::class.java).also {
                startService(it) // bukan Star Activity, tapi service
                tvServiceInfo.text = "Service is Running..."
            }
        }

        btnStopService.setOnClickListener {
            Intent(this, MyService::class.java).also {
                stopService(it)
                tvServiceInfo.text = "Service is Stopped..."
            }
        }

        btnSendData.setOnClickListener {
            Intent(this, MyService::class.java).also{
                val dataString = etData.text.toString()
                it.putExtra("EXTRA_DATA", dataString)
                startService(it)
            }
        }
    }
}