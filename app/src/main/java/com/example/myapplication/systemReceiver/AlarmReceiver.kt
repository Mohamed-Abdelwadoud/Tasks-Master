package com.example.myapplication.systemReceiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import com.example.myapplication.service.AlarmMangerService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hint = intent.getStringExtra("Hint")

        val dailyAlarmServiceIntent = Intent(context, AlarmMangerService::class.java)
        dailyAlarmServiceIntent.putExtra("Hint", hint)
        startForegroundService(context, dailyAlarmServiceIntent)

    }


}
