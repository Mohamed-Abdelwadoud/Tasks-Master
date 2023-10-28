package com.example.myapplication.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.ActivityToDayBinding
import com.example.myapplication.service.AlarmMangerService
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.utilities.ListSwipeAction
import com.example.myapplication.utilities.TasksPressAction
import com.example.myapplication.viewModel.TasksViewModel

class ToDayActivity : AppCompatActivity(), TaskAdapter.SingleTaskListener,
    TaskAdapter.GroupNameListener {
    private lateinit var binding: ActivityToDayBinding
    private lateinit var taskAdaptor: TaskAdapter
    private lateinit var mTasksViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDayBinding.inflate(layoutInflater)
        taskAdaptor = TaskAdapter(this, this)

        setContentView(binding.root)
        binding.topLayout.filterICON.visibility = View.GONE



        initUI()
        binding.topLayout.passedTasksICOn.setOnClickListener {
            val a = Intent(this, PassedAllTasksActivity::class.java)
            startActivity(a)
            finish()
        }

        binding.topLayout.toDayICON.setOnClickListener {
            Toast.makeText(baseContext, "This is ToDays Tasks", Toast.LENGTH_SHORT).show()
        }
        binding.topLayout.settingsICON.setOnClickListener {
            val a = Intent(this, SettingsActivity::class.java)
            startActivity(a)
            finish()
        }
        binding.topLayout.achievementICON.setOnClickListener {
            val a = Intent(this, AchievementActivity::class.java)
            startActivity(a)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        stopService()
    }


    private fun initUI() {

        binding.TasksRecycler.layoutManager = LinearLayoutManager(this)
        binding.TasksRecycler.adapter = taskAdaptor
        mTasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        mTasksViewModel.getTasks.observe(this, Observer { tasks ->
            taskAdaptor.addTasks(tasks)
            taskAdaptor.showToDay(true)
            if (taskAdaptor.getAllTasks() == 0) {
                binding.mainTxt.text = "No Dead line for today "
            }
        })
        ListSwipeAction.getItemTouchHelper(this, mTasksViewModel)
            .attachToRecyclerView(binding.TasksRecycler)

        //  mTasksViewModel.deleteAllTasks()

    }


    override fun handleGroupClick(groupName: String) {

        val intent = Intent(this, CreatePlanActivity::class.java)
        intent.putExtra("group", groupName)
        startActivity(intent)


    }


    override fun handlePress(taskModel: TaskModel?) {
        TasksPressAction(mTasksViewModel, taskModel!!, this).testhandelpress()

    }


    private fun stopService() {
        val intent = Intent(baseContext, AlarmMangerService::class.java)
        stopService(intent)
    }

}