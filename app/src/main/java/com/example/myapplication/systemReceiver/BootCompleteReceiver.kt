package com.example.myapplication.systemReceiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("SDSDSDSDSDSDSDSDSDSDSD", "onReceive: ACTION_BOOT_COMPLETED")

            if (context.getSharedPreferences("dailySwitchPreferences", Context.MODE_PRIVATE)
                    .getBoolean("DailySwitch", false)
            ) {
                Log.d("SDSDSDSDSDSDSDSDSDSDSD", "onReceive: dailySwitchPreferences On")
                Thread {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager // now
                    val intentAAA = Intent(context, AlarmReceiver::class.java)
                    intentAAA.putExtra("Hint", "DailyAlarmTrigger")
                    val pendingIntent =
                        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    val calendar = Calendar.getInstance()
                    val dailyAlarmPreferences = context.getSharedPreferences(
                        "dailyAlarmPreferences",
                        AppCompatActivity.MODE_PRIVATE
                    )

                    val pickedHour = dailyAlarmPreferences.getInt("hour", 0)
                    val pickedMinute = dailyAlarmPreferences.getInt("min", 0)
                    Log.d("SDSDSDSDSDSDSDSDSDSDSD", "AlarmValues: $pickedHour ::::: $pickedMinute")
                    calendar.set(Calendar.HOUR_OF_DAY, pickedHour)
                    calendar.set(Calendar.MINUTE, pickedMinute)
                    calendar.set(Calendar.SECOND, 0)
                    val triggerTime = calendar.timeInMillis

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Log.d("SDSDSDSDSDSDSDSDSDSDSD", "onReceive: Case A ")

                        if (alarmManager.canScheduleExactAlarms()) {
                            Log.d("SDSDSDSDSDSDSDSDSDSDSD", "onReceive: Case B ")

                            alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                AlarmManager.INTERVAL_DAY,
                                pendingIntent
                            )
                        }
                    } else {
                        Log.d("SDSDSDSDSDSDSDSDSDSDSD", "onReceive: Case c ")
                        alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                        )

                    }
                }.start()

            }
        }
    }
}