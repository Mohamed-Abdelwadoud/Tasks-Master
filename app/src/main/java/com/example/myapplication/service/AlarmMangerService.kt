package com.example.myapplication.service

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.ui.MainActivity

class AlarmMangerService : Service() {
    private var myMediaPlayer: MediaPlayer? = null
    private val channelID = "SINGLE_ALARM_Channel"
    private lateinit var notificationManager: NotificationManager
    private lateinit var hint: String
    private val dailyChannelId = "Daily_ALARM_Channel"
    private lateinit var builder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val i = Intent(baseContext, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivities(baseContext, 0, arrayOf(i), PendingIntent.FLAG_IMMUTABLE)

        builder = NotificationCompat.Builder(baseContext, channelID)
            .setSmallIcon(R.drawable.ic_baseline_alarm_24)
            .setContentTitle("Alarm")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        hint = intent?.getStringExtra("Hint").toString()
        if (hint == "DailyAlarmTrigger") {
            builder.setContentText("This is reminder for To Day Tasks")
            dailyAlarmAction()
        } else {
            builder.setContentText("This is reminder for $hint Task")
            singleAlarmAction()
        }
        startForeground(11, builder.build())
        return START_NOT_STICKY

    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun pushNotificationAndPlaySound() {
        val switchSharedPreferences = getSharedPreferences("ringToneSwitch", MODE_PRIVATE)
        if (switchSharedPreferences.getBoolean("ringToneSwitch", false)) {
            if (myMediaPlayer != null) {
                if (myMediaPlayer?.isPlaying == true) {
                    myMediaPlayer?.stop()
                    myMediaPlayer?.reset()
                    myMediaPlayer?.release()

                }
                myMediaPlayer = null
            }

            myMediaPlayer = MediaPlayer()
            val soundFilePreferences = baseContext.getSharedPreferences(
                "soundFilePreferences",
                AppCompatActivity.MODE_PRIVATE
            )
            val sourceFile = soundFilePreferences.getString("Source", "")
            if (!sourceFile.isNullOrBlank()) {
                myMediaPlayer?.setDataSource(sourceFile)
                myMediaPlayer?.prepare()
            } else {
                myMediaPlayer =
                    MediaPlayer.create(baseContext, R.raw.sound) as MediaPlayer
            }

            Thread {
                myMediaPlayer?.start()
            }.start()

            myMediaPlayer?.setOnCompletionListener {
                myMediaPlayer?.release()
                stopSelf()
            }
        }


    }

    override fun onDestroy() {
        if (myMediaPlayer!=null){
            myMediaPlayer?.release()
        }
        notificationManager.notify(123,builder.build())
        super.onDestroy()
    }
    private fun dailyAlarmAction (){
        Log.d("SDSDSDSDSDSDSDSDSDSDSD", "dailyAlarmAction: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var dailyAlarmChannel = notificationManager.getNotificationChannel(dailyChannelId)
            if (dailyAlarmChannel == null) {
                dailyAlarmChannel =
                    NotificationChannel(
                        dailyChannelId,
                        "Single Task ALARM",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                dailyAlarmChannel.description = "FOR Daily Reminder"
                notificationManager.createNotificationChannel(dailyAlarmChannel)
            }
        }
        stopSelf()
    }


    private fun singleAlarmAction(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var singleAlarmChannel = notificationManager.getNotificationChannel(channelID)
            if (singleAlarmChannel == null) {
                singleAlarmChannel =
                    NotificationChannel(
                        channelID,
                        "Single Task ALARM",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                singleAlarmChannel.description = "FOR Single ALARM"
                notificationManager.createNotificationChannel(singleAlarmChannel)
            }

        }
        pushNotificationAndPlaySound()

    }


}


