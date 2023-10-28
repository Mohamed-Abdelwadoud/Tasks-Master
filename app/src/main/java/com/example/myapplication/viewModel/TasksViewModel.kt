package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.data.source.local.TasksDataBase
import com.example.myapplication.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    val getTasks: LiveData<List<TaskModel>>
    private val repository: TasksRepository



    init {
        val taskDao = TasksDataBase.getTasksDataBase(application.baseContext).getTasksDao()
        repository = TasksRepository(taskDao)
        getTasks = repository.readTasks


    }

    fun addNewTask(taskModel: TaskModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(taskModel)
        }
    }

    fun deleteAllTasks() {

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllData()
        }
    }

    fun addNewPlan(planTasks: List<TaskModel>) {
        viewModelScope.launch(Dispatchers.IO)
        { repository.insertPlan(planTasks) }
    }

    suspend fun checkHeaderExisted(header: String): Boolean {

        return repository.isHeaderExisted(header)
    }

    suspend fun deleteAGroup(groupName: String){
        repository.deleteGroup(groupName)
    }


    suspend fun checkGroupNameExisted(groupName: String): Boolean {

        return repository.isGroupNameExisted(groupName)
    }




    suspend fun deleteSingleHeader(header: String){
        repository.deleteHeader(header)
    }


    suspend fun updateTaskDeadLine(lastDate : Calendar,remainTime:String, header: String){
        repository.updateTask(lastDate,remainTime,header)
    }


    suspend fun setTaskDone(isDone: Boolean,header: String){
        repository.taskISDone(isDone,header)
    }





}