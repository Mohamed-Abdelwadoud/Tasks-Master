package com.example.myapplication.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
class Converts {
    @TypeConverter

    fun toGson(cal: Calendar): String {
        val Gson =Gson()
        return Gson.toJson(cal)
    }
    @TypeConverter

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromGson(cal:String ): Calendar {
        val Gson =Gson()
        val listType: Type = object : TypeToken<Calendar>() {}.type

        return Gson.fromJson(cal,listType)
    }
}