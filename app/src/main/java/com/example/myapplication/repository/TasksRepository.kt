package com.example.myapplication.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.data.source.local.TaskDao
import java.util.Calendar

class TasksRepository(private val taskDao: TaskDao) {

    var readTasks: LiveData<List<TaskModel>> = taskDao.getLiveTasks()

    suspend fun addTask(task: TaskModel) {
        taskDao.insertSingleTask(task)
    }

   suspend fun deleteAllData(){
        taskDao.deleteAll()
    }

    suspend fun insertPlan (planTasks: List<TaskModel>){
        taskDao.insertPlanTasks(planTasks)
    }


    suspend fun deleteGroup (groupName: String){
        taskDao.deleteAllGroup(groupName)
    }
    suspend fun isHeaderExisted(header:String):Boolean{
       return taskDao.headerExisted(header)
    }


    suspend fun isGroupNameExisted(groupName:String):Boolean{
        return taskDao.groupNameExisted(groupName)
    }



    suspend fun deleteHeader(header: String){
        taskDao.deleteByHeader(header)
    }

    suspend fun updateTask(lastDate:Calendar,remainTime:String,header: String){
        taskDao.updateDeadLine(lastDate,remainTime,header)
    }

    suspend fun taskISDone(isDone: Boolean, header: String){
        taskDao.makeTaskDone(isDone,header)
    }

     fun getDoneTasks(isDone: Boolean):LiveData<List<TaskModel>>{
         return taskDao.getALLDone(isDone)
    }


}