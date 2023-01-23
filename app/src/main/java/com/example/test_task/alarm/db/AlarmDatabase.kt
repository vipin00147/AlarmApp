package com.example.test_task.alarm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao?

    companion object {
        private var instance: AlarmDatabase? = null
        @Synchronized
        fun getInstance(context: Context): AlarmDatabase? {

            if (instance == null) {

                instance = Room.databaseBuilder(context.applicationContext,
                        AlarmDatabase::class.java,
                        "alarm_database")
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance
        }
    }
}