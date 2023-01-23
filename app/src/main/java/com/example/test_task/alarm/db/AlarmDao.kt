package com.example.test_task.alarm.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alarmEntity: AlarmEntity?)

    @Update
    fun update(alarmEntity: AlarmEntity?)

    @get:Query("SELECT * FROM alarm_table ORDER BY mAlarmTime ASC")
    val allAlarms: LiveData<List<AlarmEntity>>?

    @get:Query("SELECT * FROM alarm_table")
    val allAlarmsReSched: List<AlarmEntity>?

    @Query("DELETE FROM alarm_table WHERE mAlarmId = :id")
    fun deleteAlarm(id: Int)

    @Query("UPDATE alarm_table SET mAlarmEnabled = :isAlarmEnabled WHERE mAlarmId = :id")
    fun updateAlarmStatus(id: Int, isAlarmEnabled: Boolean)

    @Query("UPDATE alarm_table SET mAlarmId=:newAlarmId, mAlarmTime=:alarmTime WHERE mAlarmId=:oldAlarmId")
    fun updateAlarmIdTime(oldAlarmId: Int, newAlarmId: Int, alarmTime: Long)

    @Query("SELECT * FROM alarm_table WHERE mAlarmId=:id")
    fun getAlarm(id: Int): AlarmEntity?

    @Query("UPDATE alarm_table SET mAlarmTitle = :alarmTitle WHERE mAlarmId = :alarmId")
    fun setAlarmTitle(alarmTitle: String?, alarmId: Int)
}