package com.example.myapplication.ui.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.databinding.TaskItemLayoutBinding
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class TaskAdapter(groupNameListener: GroupNameListener?, singleTaskListener: SingleTaskListener?) :
    RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    private lateinit var mGroupNameListener: GroupNameListener
    private lateinit var mSingleTaskListener: SingleTaskListener
    private var tasks: ArrayList<TaskModel>? = null
    private var createTasks: ArrayList<TaskModel> = ArrayList()
    private val toDay = Calendar.getInstance()
    private var mOnlyToday = false


    init {

        if (groupNameListener != null) {
            mGroupNameListener = groupNameListener
        }
        if (singleTaskListener != null) {
            mSingleTaskListener = singleTaskListener
        }
        cleatTimeComponent(toDay)
        notifyDataSetChanged()
    }


    fun showPassed() {
        tasks!!.removeAll { it.Done }
        tasks!!.removeAll {
            it.LastDate.get(Calendar.YEAR) != toDay.get(Calendar.YEAR)
        }
        tasks!!.removeAll {
            it.LastDate.get(
                Calendar.DAY_OF_YEAR
            ) >= (toDay.get(Calendar.DAY_OF_YEAR))
        }
        notifyDataSetChanged()
    }


    fun addTasks(array: List<TaskModel>) {
        this.tasks = (ArrayList(array))
        tasks!!.apply {
            sortBy { it.LastDate }
            sortBy { it.GroupName }
        }
        notifyDataSetChanged()

    }


    fun showToDay(boolean: Boolean) {
        mOnlyToday = boolean
        // this might have a conflict if days are equal with diff years >> if tiny if
        tasks!!.removeAll {
            it.LastDate.get(Calendar.YEAR) != toDay.get(Calendar.YEAR) || it.LastDate.get(
                Calendar.DAY_OF_YEAR
            ) != (toDay.get(Calendar.DAY_OF_YEAR))
        }

    }

    fun getAllTasks(): Int {
        return tasks!!.size
    }

    fun doneFilter() {
        tasks?.removeAll { !it.Done }
        notifyDataSetChanged()

    }

    fun showNotDone() {
        tasks?.removeAll { it.Done }
        notifyDataSetChanged()

    }

    fun showGroup(groupName: String) {
        tasks!!.removeAll { it.GroupName != groupName }
        createTasks = tasks as ArrayList<TaskModel>
        tasks = null
    }

    fun updateGroup(newGroupName: String) {
        createTasks.forEach { it.GroupName = newGroupName }
    }

    fun getPlan(): ArrayList<TaskModel> {
        return this.createTasks
    }

    fun addToPlan(taskModel: TaskModel) {
        createTasks.add(taskModel)
//        if(createTasks.isNullOrEmpty()){
//            tasks?.add(taskModel)
//        }else{
//            createTasks.add(taskModel)
//
//        }
        notifyDataSetChanged()
    }

    fun killCreation() {
        createTasks?.clear()
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val binding =
            TaskItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        if (!tasks.isNullOrEmpty()) {
            if (position == 0) {
                holder.binding.breakLine.visibility = View.VISIBLE
            } else if (position != 0 && tasks!![position].GroupName != tasks!![position - 1].GroupName) {
                holder.binding.breakLine.visibility = View.VISIBLE
            } else {
                holder.binding.breakLine.visibility = View.GONE
            }

            holder.binding.groupName.text = tasks?.get(position)?.GroupName
            holder.binding.taskHeader.text = tasks?.get(position)?.Header
            holder.binding.taskInfo.text = tasks?.get(position)?.Info
            holder.binding.DeadLine.text = tasks?.get(position)?.RemainTime
            if (mOnlyToday) {
                holder.binding.remainTime.text = "0 Days left"
            } else if (tasks?.get(position)?.Done == true) {
                holder.binding.remainTime.text = "task is done"
            } else {
                holder.binding.remainTime.text =
                    "${tasks?.get(position)?.LastDate?.let { daysLift(it) }} Days left"
            }


        }

        if (createTasks.size > 0) {
            createTasks.sortBy { it.LastDate }
            with(holder.binding) {
                taskHeader.text = createTasks[position]?.Header
                taskInfo.text = createTasks[position]?.Info
                DeadLine.text = createTasks[position]?.RemainTime
                if (createTasks[position]?.Done == true) {
                    remainTime.text = "task is done"

                } else {
                    remainTime.text = "${daysLift(createTasks[position].LastDate)} Days Left"

                }


            }
        }

    }

    override fun getItemCount(): Int {
        return if (tasks.isNullOrEmpty()) {
            createTasks.size
        } else tasks?.size ?: 0

    }

    inner class TaskHolder(val binding: TaskItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun layOutItem(): TaskModel {
            return (tasks?.get(layoutPosition)!!)
        }


        init {


            binding.root.setOnClickListener {
                mSingleTaskListener.handlePress(tasks?.get(layoutPosition))
            }

            binding.breakLine.setOnClickListener {
                mGroupNameListener.handleGroupClick(tasks!![layoutPosition].GroupName)
            }


        }


    }


    private fun daysLift(endDate: Calendar): Int {
        cleatTimeComponent(toDay)
        cleatTimeComponent(endDate)
        val time = endDate.timeInMillis - toDay.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(time).toInt()
    }

    private fun cleatTimeComponent(date: Calendar) {
        date.clear(Calendar.HOUR)
        date.clear(Calendar.AM_PM)
        date.clear(Calendar.HOUR_OF_DAY)
        date.clear(Calendar.SECOND)
        date.clear(Calendar.MILLISECOND)
        date.clear(Calendar.MINUTE)
    }


    interface GroupNameListener {
        fun handleGroupClick(groupName: String)
    }

    interface SingleTaskListener {
        fun handlePress(taskModel: TaskModel?)

    }

}

