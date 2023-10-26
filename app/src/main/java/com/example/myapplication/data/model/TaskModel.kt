package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TaskModel(
    var Done:Boolean,
    var GroupName:String,
    @PrimaryKey val Header: String,
    val  Info: String,
    val RemainTime:String,
    val LastDate: java.util.Calendar

)