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
import com.example.myapplication.databinding.ActivityAchievementBinding
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.viewModel.TasksViewModel
import java.util.*
import kotlin.collections.ArrayList

class AchievementActivity : AppCompatActivity(), TaskAdapter.GroupNameListener {
    private lateinit var binding: ActivityAchievementBinding
    private lateinit var taskAdaptor: TaskAdapter
    private lateinit var mTasksViewModel: TasksViewModel
    private var array: ArrayList<TaskModel> = ArrayList()
    private val toDay = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskAdaptor = TaskAdapter(this, null)

        binding.topLayout.filterICON.visibility = View.GONE


        initUI()
        binding.topLayout.passedTasksICOn.setOnClickListener {
            val a = Intent(this, PassedAllTasksActivity::class.java)
            startActivity(a)
            finish()
        }



        binding.topLayout.toDayICON.setOnClickListener {
            val a = Intent(this, ToDayActivity::class.java)
            startActivity(a)
            finish()
        }
        binding.topLayout.settingsICON.setOnClickListener {
            val a = Intent(this, SettingsActivity::class.java)
            startActivity(a)
            finish()

        }

        binding.topLayout.achievementICON.setOnClickListener {
            Toast.makeText(baseContext, "This is achievement ", Toast.LENGTH_SHORT).show()

        }

    }


    private fun initUI() {
        binding.TasksRecycler.layoutManager = LinearLayoutManager(this)
        mTasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        binding.TasksRecycler.adapter = taskAdaptor
        mTasksViewModel.getTasks.observe(this, Observer { tasks ->
            taskAdaptor.addTasks(tasks)
            array = tasks as ArrayList<TaskModel>
            taskAdaptor.doneFilter()
            updateProgressParsView()
        })

    }


    override fun handleGroupClick(groupName: String) {
        val intent = Intent(this, CreatePlanActivity::class.java)
        intent.putExtra("group", groupName)
        startActivity(intent)

    }


    private fun updateProgressParsView() {
        val toDayCount = array.count {
            it.LastDate.get(Calendar.YEAR) == toDay.get(Calendar.YEAR) && it.LastDate.get(
                Calendar.DAY_OF_YEAR
            ) == (toDay.get(Calendar.DAY_OF_YEAR))
        }
        val toDayCountDone = array.count {
            it.LastDate.get(Calendar.YEAR) == toDay.get(Calendar.YEAR) && it.LastDate.get(
                Calendar.DAY_OF_YEAR
            ) == (toDay.get(Calendar.DAY_OF_YEAR)) && it.Done
        }
        val allTasksCount = array.size
        val allTasksDone = array.count { it.Done }

        binding.allprogressBar.max = allTasksCount

        binding.allprogressBar.progress = allTasksDone

        binding.todayprogressBar.max = toDayCount

        binding.todayprogressBar.progress = toDayCountDone


        binding.toDayAchievementTV.append(" $toDayCountDone of $toDayCount  ")
        binding.allAchievementTV.append(" $allTasksDone of $allTasksCount  ")

    }


}