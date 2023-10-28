package com.example.myapplication.data.source.local

import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.model.TaskModel
import java.util.Calendar

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleTask(task: TaskModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanTasks(planTasks: List<TaskModel>)

    @Query("select * from TaskModel ")
    fun getLiveTasks(): LiveData<List<TaskModel>>

    @Query("delete from TaskModel")
    suspend fun deleteAll()

    @Query("update TaskModel set LastDate =:lastDate, RemainTime =:remainTime where Header =:header")
    suspend fun updateDeadLine( lastDate :Calendar, remainTime:String ,header: String)

    @Query("delete from TaskModel where Header =:header")
    suspend fun deleteByHeader(header:String)

    @Query("delete from TaskModel where GroupName =:groupName")
    suspend fun deleteAllGroup(groupName:String)

    @Query("select EXISTS (Select Header from TaskModel where Header = :header )")
    suspend fun headerExisted(header: String?): Boolean

    @Query("select EXISTS (Select GroupName from TaskModel where GroupName = :group )")
    suspend fun groupNameExisted(group: String?): Boolean

    @Query("UPDATE TaskModel SET Done = :isDone where Header = :header")
    suspend fun makeTaskDone(isDone: Boolean,header: String)





}