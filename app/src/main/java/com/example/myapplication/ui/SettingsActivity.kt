package com.example.myapplication.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivitySettingsBinding
import com.example.myapplication.service.SoundPlayerManager
import com.example.myapplication.systemReceiver.AlarmReceiver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.*
import java.util.*


class SettingsActivity : AppCompatActivity() {
    // pref >>  ringToneSwitch {boolean ringToneSwitch }
    // pref >> soundFilePreferences >> {SourceName (String)     >>>> Source 'URI' String}
    // pref >> dailySwitchPreferences>>>>> {DailySwitch boolean }
    // pref >> dailyAlarmPreferences  {hour Int min Int}

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val editor = getSharedPreferences("ringToneSwitch", MODE_PRIVATE).edit()

        IntUI()

        // RingTone
        binding.uploadRingToneTX.setOnClickListener(View.OnClickListener {
            // do check
            if (binding.ringToneSwitch.isChecked) {
                addSoundFile()
            } else {
                Toast.makeText(this, "open Alarm Sound First", Toast.LENGTH_SHORT).show()
            }

        })
        binding.ringToneSwitch.setOnClickListener(View.OnClickListener {
            if (binding.ringToneSwitch.isChecked) {
                editor.putBoolean("ringToneSwitch", true)
                editor.apply()
                binding.RingToneName.visibility = View.VISIBLE
                binding.uploadRingToneTX.visibility = View.VISIBLE
                binding.removeToneTX.visibility = View.VISIBLE

            } else {
                editor.putBoolean("ringToneSwitch", false)
                editor.apply()
                binding.RingToneName.visibility = View.GONE
                binding.uploadRingToneTX.visibility = View.GONE
                binding.removeToneTX.visibility = View.GONE
            }
        })
        binding.RingToneName.setOnClickListener(View.OnClickListener {
            // play sound for test
            if (SoundPlayerManager.getInstance().isMediaPlaying()) {
                SoundPlayerManager.getInstance().resetMedia()

            } else {
                SoundPlayerManager.getInstance().playMedia(this)
            }
        })
        binding.removeToneTX.setOnClickListener(View.OnClickListener {
            val soundFilePreferences = getSharedPreferences("soundFilePreferences", MODE_PRIVATE)
            soundFilePreferences.edit().clear().apply()
            binding.RingToneName.text = "Current : Basic"


        })
        // Daily Alarm
        binding.dailyReminderSwitch.setOnClickListener(View.OnClickListener {
            if (binding.dailyReminderSwitch.isChecked) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE)
                            .edit().putBoolean("DailySwitch", true).apply()
                        binding.dailyRemniderTimeRV.visibility = View.VISIBLE

                    } else {
                        binding.dailyReminderSwitch.isChecked = false
                        showExactSettingDialog()
                        getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE)
                            .edit().putBoolean("DailySwitch", false).apply()
                        binding.dailyRemniderTimeRV.visibility = View.GONE

                    }
                } else {
                    getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE)
                        .edit().putBoolean("DailySwitch", true).apply()
                    binding.dailyRemniderTimeRV.visibility = View.VISIBLE
                    binding.triggerTime.text="No Time Set "
                    binding.triggerTime.visibility=View.VISIBLE

                }
            } else {
                getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE)
                    .edit().putBoolean("DailySwitch", false).apply()


                binding.dailyRemniderTimeRV.visibility = View.GONE
                binding.triggerTime.visibility=View.GONE
                killDailyReminder()

            }

        })
        binding.dailyRemniderTimeRV.setOnClickListener(View.OnClickListener {

            killDailyReminder()
            setScheduleAlarm()
        })

    }


    override fun onPause() {
        super.onPause()
        SoundPlayerManager.getInstance().resetMedia()
    }


    private var readMediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    showSettingDialog()
                }
            } else {
                Toast.makeText(baseContext, "Granted", Toast.LENGTH_SHORT).show()
                getSoundFile.launch("audio/*")
            }
        }

    private var getSoundFile: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // can start a dialog here

                val cachedFile: File? = saveSoundFileToCache(this, uri)
                if (cachedFile != null) {
                    val soundFilePreferences =
                        getSharedPreferences("soundFilePreferences", MODE_PRIVATE)
                    val soundFileEditor =
                        getSharedPreferences("soundFilePreferences", MODE_PRIVATE).edit()
                    soundFileEditor.putString("Source", "${cachedFile.toURI()}")
                    soundFileEditor.apply()
                    Log.d(
                        "AAAAAAAAAAAAAAA", "file Source ${
                            soundFilePreferences.getString("Source", null)
                        }"
                    )
                } else {
                    Toast.makeText(baseContext, "Failed To Upload File ", Toast.LENGTH_SHORT).show()
                }


                // Name set
                val soundFileName: String? = getSoundFileName(this, uri)
                if (soundFileName != null) {
                    // Process the sound file name
                    binding.RingToneName.text = soundFileName
                    val soundFileEditor =
                        getSharedPreferences("soundFilePreferences", MODE_PRIVATE).edit()
                    soundFileEditor.putString("SourceName", soundFileName)
                        .apply()
                }
            }
        }

    private fun addSoundFile() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getSoundFile.launch("audio/*")

            // activity result
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            // show ui Explain
            showUI(
                "External Storage Permission",
                "App need Permission upload file ",
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {

            readMediaPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Access Storage Permission")
            .setMessage("Access Storage permission is required, Please allow Access Storage permission from setting")
            .setPositiveButton("go to Setting ") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:com.example.myapplication")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun IntUI() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val ringToneSwitchPreferences = getSharedPreferences("ringToneSwitch", MODE_PRIVATE)
        val soundFilePreferences = getSharedPreferences("soundFilePreferences", MODE_PRIVATE)
        if (ringToneSwitchPreferences.getBoolean("ringToneSwitch", false)) {
            binding.RingToneName.visibility = View.VISIBLE
            binding.uploadRingToneTX.visibility = View.VISIBLE
            binding.removeToneTX.visibility = View.VISIBLE
            binding.ringToneSwitch.isChecked = true
        } else {
            binding.removeToneTX.visibility = View.GONE

            binding.RingToneName.visibility = View.GONE
            binding.uploadRingToneTX.visibility = View.GONE
            binding.removeToneTX.visibility = View.GONE

        }
        if (soundFilePreferences.getString("SourceName", "").isNullOrBlank()) {
            binding.RingToneName.text = "Current : Basic"
        } else {
            binding.RingToneName.text =
                "${soundFilePreferences.getString("SourceName", "")} "
        }
        binding.dailyReminderSwitch.isChecked =
            getSharedPreferences("dailySwitchPreferences", MODE_PRIVATE).getBoolean(
                "DailySwitch",
                false
            )
        if (binding.dailyReminderSwitch.isChecked) {
            binding.dailyRemniderTimeRV.visibility = View.VISIBLE
            if (getSharedPreferences("dailyAlarmPreferences", MODE_PRIVATE)
                    .getBoolean("Added", false)){
                binding.triggerTime.visibility=View.VISIBLE
                val hour = getSharedPreferences("dailyAlarmPreferences", MODE_PRIVATE)
                    .getInt("hour", 0)
                val min = getSharedPreferences("dailyAlarmPreferences", MODE_PRIVATE)
                    .getInt("min", 0)
                binding.triggerTime.text="${ hour }: ${ min }"

            }else{
                binding.triggerTime.text="No Time Set "
                binding.triggerTime.visibility=View.VISIBLE

            }
        } else {
            binding.dailyRemniderTimeRV.visibility = View.GONE
            binding.triggerTime.visibility=View.GONE
        }



    }

    // all about Sound File
    private fun getSoundFileName(context: Context, soundUri: Uri): String? {
        val contentResolver: ContentResolver = context.contentResolver
        var cursor: Cursor? = null
        var soundFileName: String? = null

        try {
            cursor = contentResolver.query(soundUri, null, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val nameIndex: Int = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        soundFileName = it.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return soundFileName
    }

    private fun showUI(title: String, message: String, permission: String) {
        Log.d("AAAAAAAAAAAAAAA", "case UI ")

        val alertdialog = AlertDialog.Builder(this)
        alertdialog.setMessage(message)
        alertdialog.setTitle(title)
        alertdialog.setCancelable(false)
        alertdialog.setPositiveButton(
            "Okay"
        ) { dialog, which ->
            dialog.dismiss()
            readMediaPermissionLauncher.launch(
                permission
            )
        }
        alertdialog.show()
    } // end of Explain message

    private fun saveSoundFileToCache(context: Context, soundUri: Uri): File? {
        val contentResolver = context.contentResolver
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var cachedFile: File? = null
        var progressDialog = AlertDialog.Builder(context)
            .setMessage("Reading sound file Please Wait...")
            .setCancelable(false)
            .create()
        progressDialog?.show()
        try {
            inputStream = contentResolver.openInputStream(soundUri)
            val cacheDir = context.cacheDir
            cachedFile = File(cacheDir, "sound_file")
            outputStream = FileOutputStream(cachedFile)

            inputStream?.copyTo(outputStream)
        } catch (e: Exception) {
            progressDialog.dismiss()
            e.printStackTrace()
        } finally {
            progressDialog.dismiss()
            inputStream?.close()
            outputStream?.close()
        }

        return cachedFile
    }

    //                val soundData: ByteArray? = readSoundFileContent(this, uri)
//                if (soundData != null) {
//                    val ringToneByteSharedPreferences =
//                        getSharedPreferences("ringToneByte", MODE_PRIVATE)
//                    val editor = getSharedPreferences("ringToneByte", MODE_PRIVATE).edit()
//                    editor.putString("ToneByte", soundData.toString())
//                    // Process the sound file data
//                }
    private fun readSoundFileContent(context: Context, soundUri: Uri): ByteArray? {
        val contentResolver: ContentResolver = context.contentResolver
        var inputStream: InputStream? = null
        var soundData: ByteArray? = null
        var progressDialog = AlertDialog.Builder(context)
            .setMessage("Reading sound file Please Wait...")
            .setCancelable(false)
            .create()
        progressDialog?.show()
        try {
            inputStream = contentResolver.openInputStream(soundUri)
            if (inputStream != null) {
                soundData = inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            progressDialog.dismiss() // Dismiss progress dialog

        } finally {
            inputStream?.close()
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }// Dismiss progress dialog
        }

        return soundData
    }


    /// about EXACTAlarmPermission
    private fun checkEXACTAlarmPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
//               startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse(BuildConfig.APPLICATION_ID)))
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:com.example.myapplication")
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showExactSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Alarm&Reminder Permission")
            .setMessage(" permission is required, Please allow Access Storage permission from setting")
            .setPositiveButton("go to Setting ") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:com.example.myapplication")
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.dailyReminderSwitch.isChecked = false
            }
            .show()
    }

    private fun setScheduleAlarm() {
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Set Time")
            .build()
        materialTimePicker.show(supportFragmentManager, "SettingActivity")
        materialTimePicker.addOnPositiveButtonClickListener(View.OnClickListener {
            val pickedHour: Int = materialTimePicker.hour
            val pickedMinute: Int = materialTimePicker.minute
            createSchAlarm(pickedHour, pickedMinute)
            getSharedPreferences("dailyAlarmPreferences", MODE_PRIVATE)
                .edit().putInt("hour", pickedHour).putInt("min", pickedMinute)
                .putBoolean("Added", materialTimePicker.isAdded).apply()
            binding.triggerTime.text="${ pickedHour }: ${ pickedMinute }"
            binding.triggerTime.visibility=View.VISIBLE


        })

    }

    private fun killDailyReminder() {
        binding.triggerTime.text=""
        getSharedPreferences("dailyAlarmPreferences", MODE_PRIVATE)
            .edit().putInt("hour", 0).putInt("min", 0).putBoolean("Added", false).apply()


        Log.d("AAAAAAAAAAAAAAAAA", "killDailyReminder: ")
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("Hint", "DailyAlarmTrigger")
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)

    }

    private fun createSchAlarm(pickedHour: Int, pickedMinute: Int) {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("Hint", "DailyAlarmTrigger")
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, pickedHour)
        calendar.set(Calendar.MINUTE, pickedMinute)
        calendar.set(Calendar.SECOND, 0)
        val triggerTime = calendar.timeInMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )


    }


}