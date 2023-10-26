package com.example.myapplication.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.model.Converts
import com.example.myapplication.data.model.TaskModel

@Database(entities = [TaskModel::class], version = 1, exportSchema = false)
@TypeConverters(Converts::class)

abstract class TasksDataBase : RoomDatabase() {

    abstract fun getTasksDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TasksDataBase? = null
        fun getTasksDataBase(context: Context): TasksDataBase {
            val tempInstance = instance
            if (tempInstance == null) {
                synchronized(this) {
                    val instant = Room.databaseBuilder(
                        context.applicationContext,
                        TasksDataBase::class.java,
                        name = "TasK_ROOM_DB"
                    ).build()
                    instance = instant
                    return instant
                }

            } else return tempInstance

        }

    }


}