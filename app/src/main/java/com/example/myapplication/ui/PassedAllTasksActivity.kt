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


class PassedTasksActivity : AppCompatActivity(), TaskAdapter.TasksListener {
    private lateinit var binding: ActivityPassedtasksBinding
    private val taskAdaptor = TaskAdapter(this)
    private lateinit var mTasksViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassedtasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()


        binding.topLayout.passedTasksICOn.setOnClickListener(View.OnClickListener {
            Toast.makeText(baseContext, "This All DealLine passed Tasks", Toast.LENGTH_SHORT).show()
        })
        binding.topLayout.filterICON.visibility = View.GONE
        binding.topLayout.achievementICON.visibility = View.GONE


        binding.topLayout.toDayICON.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ToDayActivity::class.java)
            startActivity(intent)
            finish()
        })
        binding.topLayout.settingsICON.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        })


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

    override fun handleLongPress(taskModel: TaskModel?) {
    }

    override fun handleGroupClick(groupName: String) {
    }


}