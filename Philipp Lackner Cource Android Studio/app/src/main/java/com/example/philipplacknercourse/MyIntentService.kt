package com.example.philipplacknercourse

import android.app.IntentService
import android.content.Intent
import android.util.Log

class MyIntentService: IntentService("MyIntentService") {

    init {
        instance = this
    }

    // Companion object ?
    companion object {
        private lateinit var instance: MyIntentService
        var isRunning = false

        fun stopService() { // kenapa di dalam companion objek : Supaya bisa di panggil di class lain
            Log.d("MyIntentService", "Service is Stopped..")
            isRunning = false
            instance.stopSelf()
        }
    }


    override fun onHandleIntent(intent: Intent?) {
        try {
            isRunning = true
            while (isRunning){
                Log.d("MyIntentService", "Service is Running...")
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException){
            Thread.currentThread().interrupt()
        }
    }
}