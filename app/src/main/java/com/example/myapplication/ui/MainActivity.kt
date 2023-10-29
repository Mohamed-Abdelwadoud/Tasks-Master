package com.example.myapplication.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.AlarmMangerService
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.utilities.ListSwipeAction
import com.example.myapplication.utilities.TasksPressAction
import com.example.myapplication.viewModel.TasksViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.*


class MainActivity : AppCompatActivity(), TaskAdapter.SingleTaskListener,
    TaskAdapter.GroupNameListener {


    private val toDay = Calendar.getInstance()
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdaptor: TaskAdapter
    private lateinit var mTasksViewModel: TasksViewModel
    private lateinit var alarmManager: AlarmManager
    var filter = false


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            } else {
                // permission granted and can be tested
                // showTestNotification()
                // showNotification()

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskAdaptor = TaskAdapter(this, this)


        // alarmManager = baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager // now
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        initUI()
        binding.fabBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivity(intent)
        })

        binding.topLayout.filterICON.setOnClickListener(View.OnClickListener {
            if (filter) {
                initUI()
                filter = false
                Toast.makeText(baseContext, "ALL Tasks", Toast.LENGTH_SHORT).show()
            } else {
                taskAdaptor.showNotDone()
                Toast.makeText(baseContext, "NotDone Tasks", Toast.LENGTH_SHORT).show()

                filter = true

            }

        })
        binding.topLayout.toDayICON.setOnClickListener(View.OnClickListener {
            val a = Intent(this, ToDayActivity::class.java)
            startActivity(a)
        })
        binding.topLayout.settingsICON.setOnClickListener(View.OnClickListener {
            val a = Intent(this, SettingsActivity::class.java)
            startActivity(a)
        })
        binding.topLayout.achievementICON.setOnClickListener(View.OnClickListener {
            val a = Intent(this, AchievementActivity::class.java)
            startActivity(a)
        })

        binding.topLayout.passedTasksICOn.setOnClickListener(View.OnClickListener {
            val a = Intent(this, PassedAllTasksActivity::class.java)
            startActivity(a)
        })


    }

    override fun onStart() {
        super.onStart()
        ListSwipeAction.getItemTouchHelper(this, mTasksViewModel)
            .attachToRecyclerView(binding.TasksRecycler)
    }

    override fun onResume() {
        super.onResume()
        val alarmMangerServiceIntent = Intent(this, AlarmMangerService::class.java)
        stopService(alarmMangerServiceIntent)
    }

    private fun initUI() {

        binding.TasksRecycler.layoutManager = LinearLayoutManager(this)
        binding.TasksRecycler.adapter = taskAdaptor
        mTasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        mTasksViewModel.getTasks.observe(this, Observer { tasks ->
            taskAdaptor.addTasks(tasks)
        })

        toDay.clear(Calendar.HOUR)
        toDay.clear(Calendar.AM_PM)
        toDay.clear(Calendar.HOUR_OF_DAY)
        toDay.clear(Calendar.SECOND)
        toDay.clear(Calendar.MILLISECOND)
        toDay.clear(Calendar.MINUTE)
        // mTasksViewModel.deleteAllTasks()

    }


    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:com.example.myapplication")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    // for test
    private fun showTestNotification() {

        val channelId = "12345"
        val description = "Test Notification"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.lightColor = Color.Red.toArgb()

            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hello World")
            .setContentText("Test Notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources, R.drawable
                        .ic_launcher_background
                )
            )
        notificationManager.notify(12345, builder.build())
    }


    override fun handleGroupClick(groupName: String) {

        val intent = Intent(this, CreatePlanActivity::class.java)
        intent.putExtra("group", groupName)
        startActivity(intent)


    }


    override fun handlePress(taskModel: TaskModel?) {
        TasksPressAction(mTasksViewModel, taskModel!!, this).testhandelpress()
//        alertDialog = AlertDialog.Builder(this)
//        alertDialog.apply {
//            setTitle(taskModel?.Header)
//            setMessage("Action")
//            setCancelable(true)
//            setNeutralButton("Early Achieved") { _, _ ->
//                Toast.makeText(baseContext, "early achieved", Toast.LENGTH_SHORT).show()
//                addToAchieved(taskModel)
//            }
//
//            setNegativeButton("Post Dead Line") { _, _ ->
//                Toast.makeText(baseContext, "post dead line", Toast.LENGTH_SHORT).show()
//                postDeadLine(taskModel)
//
//            }
//
//
//
//            show()
//        }

    }



}


