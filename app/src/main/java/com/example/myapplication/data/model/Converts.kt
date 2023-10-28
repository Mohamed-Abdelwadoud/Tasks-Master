package com.example.myapplication.data.model


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

class Converts {
    @TypeConverter

    fun toGson(cal: Calendar): String {
        val gson = Gson()
        return gson.toJson(cal)
    }

    @TypeConverter

    fun fromGson(cal: String): Calendar {
        val gson = Gson()
        val listType: Type = object : TypeToken<Calendar>() {}.type

        return gson.fromJson(cal, listType)
    }
}