package com.example.myapplication.service

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

object SoundPlayerManager  {
    private var myMediaPlayer: MediaPlayer = MediaPlayer()


    private var instance: SoundPlayerManager? = null

    fun getInstance(): SoundPlayerManager {
        if (instance == null) {
            instance = SoundPlayerManager
        }
        return instance!!
    }


    fun playMedia(context: Context) {
        val soundFilePreferences = context.getSharedPreferences(
            "soundFilePreferences",
            AppCompatActivity.MODE_PRIVATE
        )
        val sourceFile = soundFilePreferences.getString("Source", "")
        if (!sourceFile.isNullOrBlank()) {
            myMediaPlayer.setDataSource(sourceFile)
            myMediaPlayer.prepare()
        } else {
            myMediaPlayer =
                MediaPlayer.create(context, R.raw.sound) as MediaPlayer
        }

        Thread {
            myMediaPlayer.start()
        }.start()


    }

    fun isMediaPlaying(): Boolean {
        return myMediaPlayer.isPlaying ?: false
    }


    fun resetMedia() {
        myMediaPlayer.reset()

    }

}

