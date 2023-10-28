package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.ActivityPassedtasksBinding
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.utilities.TasksPressAction
import com.example.myapplication.viewModel.TasksViewModel


class PassedAllTasksActivity : AppCompatActivity(), TaskAdapter.SingleTaskListener {
    private lateinit var binding: ActivityPassedtasksBinding
    private val taskAdaptor = TaskAdapter(null, this)
    private lateinit var mTasksViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassedtasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()


        binding.topLayout.passedTasksICOn.setOnClickListener {
            Toast.makeText(baseContext, "This All DealLine passed Tasks", Toast.LENGTH_SHORT).show()
        }
        binding.topLayout.filterICON.visibility = View.GONE

        binding.topLayout.achievementICON.setOnClickListener {
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.topLayout.toDayICON.setOnClickListener {
            val intent = Intent(this, ToDayActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.topLayout.settingsICON.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


    private fun initUI() {

        binding.TasksRecycler.layoutManager = LinearLayoutManager(this)
        binding.TasksRecycler.adapter = taskAdaptor
        mTasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        mTasksViewModel.getTasks.observe(this, androidx.lifecycle.Observer { tasks ->
            taskAdaptor.addTasks(tasks)
            taskAdaptor.showPassed()
        })

    }

    override fun handlePress(taskModel: TaskModel?) {
        TasksPressAction(mTasksViewModel, taskModel!!, this).testhandelpress()
    }


}