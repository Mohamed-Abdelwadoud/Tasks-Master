package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.ActivityCreateplanBinding
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.utilities.ListSwipeAction
import com.example.myapplication.viewModel.TasksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class CreatePlanActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityCreateplanBinding
    private val taskAdapter = TaskAdapter(null,null)
    private lateinit var mTasksViewModel: TasksViewModel
    private val toDay: Calendar = Calendar.getInstance()
    private var tempDate: Calendar? = null
    private var editMode = false
    private lateinit var action: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateplanBinding.inflate(layoutInflater)

        mTasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        ListSwipeAction.getItemTouchHelper(this, mTasksViewModel)
            .attachToRecyclerView(binding.multiRecyclerView)

        setContentView(binding.root)
        action = intent.getStringExtra("group").toString()
        if (action != "null") {
            showGroup(action)
            editMode = true
        }
        binding.multiRecyclerView.layoutManager = LinearLayoutManager(this.applicationContext)
        binding.multiRecyclerView.adapter = taskAdapter

        binding.DeadLine.setOnClickListener {
            onCreateDialog().show()
        }

        binding.creatSmallTasksText.setOnClickListener {
            binding.addMinTask.visibility = View.VISIBLE
            binding.creatSmallTasksText.visibility = View.GONE
        }



        binding.addMinTask.setOnClickListener {

            if (binding.mainCard.visibility == View.GONE) {
                binding.mainCard.visibility = View.VISIBLE
            } else {

                val info = binding.taskInfo.text.toString().trim()
                val deadline = binding.DeadLine.text.toString().trim()
                val headr = binding.taskHeader.text.toString().trim()
                val groupName = binding.groupNameEditText.text.toString().trim()
                if (checkFields(headr, info)
                    && checkCurrentList(headr)
                ) {
                    if (tempDate != null && dateValid(tempDate!!)) {
                        mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
                            if (!checkHeader(headr)) {
                                taskAdapter.addToPlan(
                                    TaskModel(
                                        false, groupName, headr, info, deadline,
                                        tempDate!!
                                    )
                                )
                                binding.apply {
                                    taskHeader.text.clear()
                                    taskInfo.text.clear()
                                    DeadLine.text = "Click to Set"
                                    remainTime.text = "Remain Time"


                                }
                                tempDate = null

                            } else {
                                binding.taskHeader.error = "existed"
                                makeHint("you already have another task with This header ")
                            }
                        }
                    } else binding.DeadLine.error = "Date Error"

                }
            }

        }

        binding.ConfirmTaskBtn.setOnClickListener {
            addAllTasks()
        }

        binding.groupNameCheck.setOnClickListener {
            if (binding.groupNameCheck.isChecked) {
                doGroupNameCheck()
            } else {
                binding.apply {
                    addMinTask.visibility = View.GONE
                    multiRecyclerView.visibility = View.GONE
                    groupNameEditText.isEnabled = true;
                    mainCard.visibility = View.GONE
                    ConfirmTaskBtn.visibility = View.GONE
                    creatSmallTasksText.visibility = View.GONE
                    groupNameCheck.isChecked = false
                }
            }


        }

        binding.groupNameEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doGroupNameCheck()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


    }



    private fun goToMain() {
        startActivity(Intent(this.applicationContext, MainActivity::class.java))
        finish()
    }

    private fun makeHint(hint: String) {
        Toast.makeText(this.applicationContext, hint, Toast.LENGTH_LONG).show()
    }

    private fun checkFields(header: String, info: String): Boolean {
        return if (header.isEmpty() || info.isEmpty()) {
            makeHint("Must fill empty fields")
            false
        } else {
            true
        }
    }

    private fun checkCurrentList(header: String): Boolean {
        return if (taskAdapter.getPlan().none { it.Header == header }) {
            true
        } else {
            binding.taskHeader.error = "existed"
            makeHint("you Just created this header ")
            false
        }

    }

    private suspend fun checkHeader(header: String): Boolean {

        return mTasksViewModel.checkHeaderExisted(header)
    }

    private fun dateValid(endDate: Calendar): Boolean {
        return if (endDate.before(toDay)) {
            makeHint("Task Dead Line Cant be already missed")
            false
        } else {
            true
        }
    }

    private fun onCreateDialog(): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(this, this, year, month, day)

    }

    private fun cleatTimeComponent(date: Calendar) {
        date.clear(Calendar.HOUR)
        date.clear(Calendar.AM_PM)
        date.clear(Calendar.HOUR_OF_DAY)
        date.clear(Calendar.SECOND)
        date.clear(Calendar.MILLISECOND)
        date.clear(Calendar.MINUTE)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        binding.DeadLine.error = null
        if (tempDate == null) {
            tempDate = Calendar.getInstance()
        }
        tempDate?.set(year, month, dayOfMonth)
        val newMonth = month + 1 // this to avoid pick index problem
        val endDateString = if (dayOfMonth <= 9) {
            "0$dayOfMonth/$newMonth/$year"
        } else {
            "$dayOfMonth/$newMonth/$year"
        }

        when (tempDate?.get(Calendar.DAY_OF_WEEK)) {

            1 -> binding.DeadLine.text = "Sun $endDateString"
            2 -> binding.DeadLine.text = "Mon $endDateString"
            3 -> binding.DeadLine.text = "Tue $endDateString"
            4 -> binding.DeadLine.text = "Wen $endDateString"
            5 -> binding.DeadLine.text = "Thr $endDateString"
            6 -> binding.DeadLine.text = "Fri $endDateString"
            7 -> binding.DeadLine.text = "Sat $endDateString"
        }

        binding.remainTime.text = "${daysLift(tempDate!!)} Days"

    }

    private fun daysLift(endDate: Calendar): Int {
        cleatTimeComponent(toDay)
        cleatTimeComponent(endDate)
        val time = endDate.timeInMillis - toDay.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(time).toInt()
    }


    private suspend fun checkGroupName(groupName: String): Boolean {
        return mTasksViewModel.checkGroupNameExisted(groupName)
    }

    private fun showGroup(action: String) {
        mTasksViewModel.getTasks.observe(this, androidx.lifecycle.Observer { tasks ->
            taskAdapter.addTasks(tasks)
            taskAdapter.showGroup(action)
            binding.apply {
                groupNameEditText.setText(action)
                groupNameCheck.isChecked = true
                groupNameCheck.isChecked
                groupNameEditText.isEnabled = false
                ConfirmTaskBtn.visibility = View.VISIBLE
                addMinTask.visibility = View.VISIBLE
            }

        })
    }



    private fun doGroupNameCheck() {
        if (binding.groupNameEditText.text.toString().trim().isNotEmpty()) {
            binding.groupNameEditText.error = null
            val trim = binding.groupNameEditText.text.toString().trim()
            if (editMode && trim == action) {
                binding.multiRecyclerView.visibility = View.VISIBLE
                binding.groupNameCheck.isChecked = true
                binding.groupNameEditText.isEnabled = false
                binding.ConfirmTaskBtn.visibility = View.VISIBLE
                binding.addMinTask.visibility = View.VISIBLE
            } else {

                mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
                    if (checkGroupName(trim)) {
                        makeHint("This Group Name already existed")
                        binding.groupNameCheck.isChecked = false
                    } else {
                        binding.groupNameCheck.isChecked = true
                        if (editMode) {
                            taskAdapter.updateGroup(trim)
                        }
                        binding.multiRecyclerView.visibility = View.VISIBLE
                        binding.groupNameEditText.isEnabled = false;
                        binding.mainCard.visibility = View.VISIBLE
                        binding.ConfirmTaskBtn.visibility = View.VISIBLE
                        binding.creatSmallTasksText.visibility = View.VISIBLE
                    }
                }

            }
        } else {
            binding.groupNameEditText.error = "required field"
            binding.groupNameCheck.isChecked = false


        }


    }


    private fun addAllTasks() {
        // case 1 >> header and info is empty .i will skip date and check for adaptor list (if not empty save to  Room DB)
        if (binding.taskHeader.text.toString().trim().isEmpty() && binding.taskInfo.text.toString()
                .trim().isEmpty()
        ) {
            if (taskAdapter.getPlan().isNotEmpty()) {
                mTasksViewModel.viewModelScope.launch(Dispatchers.IO) {
                    mTasksViewModel.addNewPlan(taskAdapter.getPlan())
                }
            }
            goToMain()

            // case 2 if header and info not empty .. i will check for date and current list (all valid add to adaptor list and push to Room DB)
        } else if (checkFields(binding.taskHeader.text.toString().trim(), binding.taskInfo.text.toString().trim())) {
            if (tempDate != null) {
                if (dateValid(tempDate!!)) {
                    if (taskAdapter.getPlan().isNotEmpty()&&checkCurrentList(binding.taskHeader.text.toString().trim())){
                        // check for header and add to adaptor list and push to Room
                        mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
                            if (!checkHeader(binding.taskHeader.text.toString().trim())) {
                                taskAdapter.addToPlan(
                                    TaskModel(
                                        false, binding.groupNameEditText.text.toString(),
                                        binding.taskHeader.text.toString().trim(),
                                        binding.taskInfo.text.toString().trim(),
                                        binding.DeadLine.text.toString(),
                                        tempDate!!
                                    )
                                )
                                mTasksViewModel.addNewPlan(taskAdapter.getPlan())
                                goToMain()
                            } else {
                                makeHint("Header already existed")
                            }
                        }
                    }
                }
            } else {
                // header and info existed with no date
                binding.DeadLine.error = "Set Date"
            }
        } else {
            // header or info existed
            makeHint("field are required")

        }

    }


}





