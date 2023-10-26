package com.example.myapplication.systemReceiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AlarmStateReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action.equals("android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED")) {
            //  now ready to re-trigger alarm
            if (context?.getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE)
                    ?.getBoolean("DailySwitch", false) == true
            ) {

//                GlobalScope.launch(Dispatchers.IO) {
//                    createExactAlarm(context)
//                }
                Thread{createExactAlarm(context)}.start()

            }


        }

    }

    private fun createExactAlarm(context: Context?) {
        val alarmManager =
            context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager // now
        val intentAAA = Intent(context, AlarmReceiver::class.java)
        intentAAA.putExtra("Hint", "DailyAlarmTrigger")
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intentAAA, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance()
        val dailyAlarmPreferences = context.getSharedPreferences(
            "dailyAlarmPreferences",
            AppCompatActivity.MODE_PRIVATE
        )

        val pickedHour = dailyAlarmPreferences.getInt("hour", 0)
        val pickedMinute = dailyAlarmPreferences.getInt("min", 0)

        calendar.set(Calendar.HOUR_OF_DAY, pickedHour)
        calendar.set(Calendar.MINUTE, pickedMinute)
        calendar.set(Calendar.SECOND, 0)
        val triggerTime = calendar.timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }


    }
}