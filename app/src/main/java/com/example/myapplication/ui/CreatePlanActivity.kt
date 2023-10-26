package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.ActivityCreateplanBinding
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.utilities.ListSwipeAction
import com.example.myapplication.utilities.TasksPressAction
import com.example.myapplication.viewModel.TasksViewModel
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class CreatePlanActivity : AppCompatActivity(), TaskAdapter.TasksListener,
    DatePickerDialog.OnDateSetListener {
    // handle >> list + card with data >> confirm is delayed
    // stop accepting to day tasks
    private lateinit var binding: ActivityCreateplanBinding
    private val taskAdapter = TaskAdapter(this)
    private lateinit var mTasksViewModel: TasksViewModel
    private lateinit var toDay: Calendar
    private var tempDate = Calendar.getInstance()
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

        binding.creatSmallTasksText.setOnClickListener(View.OnClickListener {
            binding.addMinTask.visibility = View.VISIBLE
            binding.cancellallMin.visibility = View.VISIBLE
            binding.creatSmallTasksText.visibility = View.GONE
        })
        binding.cancellallMin.setOnClickListener(View.OnClickListener {
            taskAdapter.killCreation()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        binding.addMinTask.setOnClickListener(View.OnClickListener {

            if (binding.mainCard.visibility == View.GONE) {
                binding.mainCard.visibility = View.VISIBLE
            } else {

                val info = binding.taskInfo.text.toString().trim()
                val deadline = binding.DeadLine.text.toString().trim()
                val headr = binding.taskHeader.text.toString().trim()
                val groupName = binding.groupName.text.toString().trim()
                if (checkFields(headr, info)
                    && checkCurrentList(headr)
                ) {
                    if (tempDate != null && dateValid(tempDate)) {
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
                                    val view: View? = currentFocus
                                    if (view != null) {
                                        val imm: InputMethodManager =
                                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                                    }

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

        })
        // this need to edit Make insure method all list has new name
        binding.ConfirmTaskBtn.setOnClickListener(View.OnClickListener {
            val info = binding.taskInfo.text.toString().trim()
            val deadline = binding.DeadLine.text.toString().trim()
            val header = binding.taskHeader.text.toString().trim()
            val groupName = binding.groupName.text.toString().trim()
            confirmOperation(info, deadline, header, groupName)
        })

        binding.groupNameCheck.setOnClickListener(View.OnClickListener {
            if (binding.groupNameCheck.isChecked) {
                Log.d("ZZZZZZZZZZZ", "onCreate: isChecked")
                val groupName = binding.groupName.text.toString().trim()
                if (groupName.isNotEmpty()) {
                    if (editMode && groupName == action) {
                        Log.d("ZZZZZZZZZZZ", "onCreate: edite mode true")
                        binding.multiRecyclerView.visibility = View.VISIBLE
                        binding.groupNameCheck.isChecked = true
                        binding.groupName.isEnabled = false
                        binding.ConfirmTaskBtn.visibility = View.VISIBLE
//                        binding.mainCard.visibility=View.VISIBLE
                        binding.addMinTask.visibility = View.VISIBLE
                    } else {
                        Log.d("ZZZZZZZZZZZ", "onCreate: edite mode false")

                        mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
                            if (checkGroupName(groupName)) {
                                Log.d("ZZZZZZZZZZZ", "onCreate: name existed")

                                makeHint("This Group Name already existed")
                                binding.groupNameCheck.isChecked = false


                            } else {
                                Log.d("ZZZZZZZZZZZ", "onCreate: name not existed")

                                if (editMode) {
                                    taskAdapter.updateGroup(groupName)
                                }

                                Log.d("ZZZZZZZZZZZ", "onCreate: final true  ")
                                binding.multiRecyclerView.visibility = View.VISIBLE
                                binding.groupName.isEnabled = false;
                                binding.mainCard.visibility = View.VISIBLE
                                binding.ConfirmTaskBtn.visibility = View.VISIBLE
                                binding.creatSmallTasksText.visibility = View.VISIBLE
                                // binding.groupNameCheck.isChecked = true


                            }
                        }

                    }
                    binding.groupName.error = null
                } else {
                    binding.groupName.error = "U Have to create name for the list "
                    binding.groupNameCheck.isChecked = false
                }
            } else {
                Log.d("ZZZZZZZZZZZ", "onCreate: isNotChecked")
                binding.apply {
                    cancellallMin.visibility = View.GONE
                    addMinTask.visibility = View.GONE
                    multiRecyclerView.visibility = View.GONE
                    groupName.isEnabled = true;
                    mainCard.visibility = View.GONE
                    ConfirmTaskBtn.visibility = View.GONE
                    creatSmallTasksText.visibility = View.GONE
                    groupName.isClickable = false
                    groupNameCheck.isChecked = false
                }
            }


        })


    }


    override fun onStart() {
        super.onStart()
        toDay = Calendar.getInstance()
        binding.DeadLine.setOnClickListener(View.OnClickListener {
            onCreateDialog().show()
        })

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
            return false
        } else {
            return true
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
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Create a new instance of DatePickerDialog and return it
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
        tempDate.set(year, month, dayOfMonth)
        var endDateString: String? = null
        val newMonth = month + 1 // this to avoid pick index problem
        endDateString = if (dayOfMonth <= 9) {
            "0$dayOfMonth/$newMonth/$year"
        } else {
            "$dayOfMonth/$newMonth/$year"
        }

        when (tempDate.get(Calendar.DAY_OF_WEEK)) {

            1 -> binding.DeadLine.text = "Sun $endDateString"
            2 -> binding.DeadLine.text = "Mon $endDateString"
            3 -> binding.DeadLine.text = "Tue $endDateString"
            4 -> binding.DeadLine.text = "Wen $endDateString"
            5 -> binding.DeadLine.text = "Thr $endDateString"
            6 -> binding.DeadLine.text = "Fri $endDateString"
            7 -> binding.DeadLine.text = "Sat $endDateString"
        }

        binding.remainTime.text = "${daysLift(tempDate!!)} Days"
        handelDaysLift(tempDate)

    }

    private fun daysLift(endDate: Calendar): Int {
        cleatTimeComponent(toDay)
        cleatTimeComponent(endDate)
        val time = endDate.timeInMillis - toDay.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(time).toInt()
    }

    private fun handelDaysLift(endDate: Calendar) {
        cleatTimeComponent(toDay)
        cleatTimeComponent(endDate)
        val calDiff = Calendar.getInstance()
        calDiff.timeInMillis = endDate.timeInMillis - toDay.timeInMillis
    }

    private fun confirmOperation(info: String, deadline: String, headr: String, groupName: String) {
        // with this code u can check if u can add main card >> will have delay and errror if main thread cant handle opreation
//        if (checkFields(headr, info)) {
//            if (tempDate != null && dateValid(tempDate!!)) {
//                goToMain()
//                mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
//                    if (!checkHeader(headr)) {
//                        taskAdapter.addToPlan(
//                            TaskModel(
//                                false, groupName,
//                                headr,
//                                info,
//                                deadline,
//                                tempDate!!
//                            )
//                        )
//                        binding.apply {
//                            taskHeader.text.clear()
//                            taskInfo.text.clear()
//                            DeadLine.text = "Click to Set"
//                            remainTime.text = "Remain Time"
//                        }
//
//
//                        mTasksViewModel.addNewplan(taskAdapter.getPlan())
//                    } else {
//                        binding.taskHeader.error = "existed"
//                        makeHint("you already have another task with This header ")
//                    }
//                }
//
//            } else binding.DeadLine.error = "Date Error"
//        } else if (taskAdapter.getPlan().isNotEmpty()) {
//            mTasksViewModel.addNewplan(taskAdapter.getPlan())
//            goToMain()
//        }


        if (taskAdapter.getPlan().isNotEmpty()) {
            mTasksViewModel.addNewplan(taskAdapter.getPlan())
            goToMain()
        } else if (taskAdapter.getPlan().isEmpty()
            && checkFields(headr, info)
        ) {
            if (tempDate != null && dateValid(tempDate!!)) {

                mTasksViewModel.viewModelScope.launch(Dispatchers.Main) {
                    if (!checkHeader(headr)) {
                        mTasksViewModel.addNewTask(
                            TaskModel(
                                false, groupName,
                                headr,
                                info,
                                deadline,
                                tempDate!!
                            )
                        )
                        goToMain()
                    } else {
                        binding.taskHeader.error = "existed"
                        makeHint("you already have another task with This header ")
                    }
                }

            } else binding.DeadLine.error = "Date Error"
        }
    }

    private suspend fun checkGroupName(groupName: String): Boolean {
        return mTasksViewModel.checkGroupNameExisted(groupName)
    }

    private fun showGroup(action: String) {
        mTasksViewModel.getTasks.observe(this, androidx.lifecycle.Observer { tasks ->
            taskAdapter.addTasks(tasks)
            taskAdapter.showGroup(action)
            binding.apply {
                groupName.setText(action)
                groupNameCheck.isChecked = true
                groupNameCheck.isChecked
                groupName.isEnabled = false
                ConfirmTaskBtn.visibility = View.VISIBLE
                addMinTask.visibility = View.VISIBLE
            }

        })
    }

    override fun handleGroupClick(groupName: String) {

        val intent = Intent(this, CreatePlanActivity::class.java)
        intent.putExtra("group", groupName)
        startActivity(intent)


    }

    override fun handleLongPress(taskModel: TaskModel?) {
        Toast.makeText(baseContext, "${taskModel!!.Done}", Toast.LENGTH_SHORT).show()
    }

    override fun handlePress(taskModel: TaskModel?) {
        TasksPressAction(mTasksViewModel, taskModel!!, this).testhandelpress()


    }

    private fun oldcoded() {
        val simpleCallBack: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        val holder = viewHolder as TaskAdapter.TaskHolder
                        //holder.deleteTask()
                        taskAdapter.notifyDataSetChanged()
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(R.color.red)
                        .addActionIcon(R.drawable.ic_baseline_delete_24).create()
                        .decorate()
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

    }
}





