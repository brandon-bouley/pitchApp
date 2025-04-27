package com.example.pitchapp.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserConverters {
    @TypeConverter
    fun fromRatedItemList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRatedItemList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
