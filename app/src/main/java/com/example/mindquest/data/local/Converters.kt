package com.example.mindquest.data.local

import androidx.room.TypeConverter
import com.example.mindquest.data.local.entity.ActivityType

class Converters {
    @TypeConverter
    fun fromActivityType(type: ActivityType): String = type.name

    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)
}
