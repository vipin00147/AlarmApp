package com.example.test_task.alarm.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

object Converter {
    @TypeConverter
    fun fromString(value: String?): Array<Boolean> {
        val listType = object : TypeToken<Array<Boolean?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromBoolean(list: Array<Boolean?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}