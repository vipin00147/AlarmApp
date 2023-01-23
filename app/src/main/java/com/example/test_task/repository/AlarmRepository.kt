package com.example.test_task.repository

import android.app.Application
import com.example.test_task.alarm.db.AlarmDao
import androidx.lifecycle.LiveData
import com.example.test_task.alarm.db.AlarmDatabase
import java.lang.Runnable
import java.lang.InterruptedException
import com.example.test_task.alarm.AlarmHelper
import com.example.test_task.alarm.db.AlarmEntity
import com.example.test_task.utils.Constants
import com.example.test_task.utils.MyApplication.myStaticObjects.databaseExecutor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AlarmRepository(application: Application?) {
    private val alarmDao: AlarmDao
    val allAlarms: LiveData<List<AlarmEntity>>

    init {
        val alarmDatabase = AlarmDatabase.getInstance(application!!)
        alarmDao = alarmDatabase?.alarmDao()!!
        allAlarms = alarmDao.allAlarms!!
    }

    fun getAlarm(alarmId: Int): AlarmEntity? {
        return alarmDao.getAlarm(alarmId)
    }

    val allAlarmsReSched: List<AlarmEntity>
        get() = alarmDao.allAlarmsReSched!!

    fun insert(alarmEntity: AlarmEntity?) {
        databaseExecutor.execute { alarmDao.insert(alarmEntity) }
    }

    fun update(alarmEntity: AlarmEntity?) {
        databaseExecutor.execute(Runnable { alarmDao.update(alarmEntity) })
    }

    fun deleteAlarm(alarmId: Int) {
        Executors.newSingleThreadScheduledExecutor().execute { alarmDao.deleteAlarm(alarmId) }
    }

    fun updateAlarmStatus(alarmId: Int, isAlarmEnabled: Boolean) {
        databaseExecutor.execute(Runnable { alarmDao.updateAlarmStatus(alarmId, isAlarmEnabled) })
    }

    fun updateAlarmIdTime(oldAlarmId: Int, newAlarmId: Int, alarmTime: Long) {
        databaseExecutor.execute(Runnable {
            alarmDao.updateAlarmIdTime(oldAlarmId, newAlarmId, alarmTime)
            updateAlarmStatus(newAlarmId, true)
            reEnableAlarmChild(newAlarmId)
        })
    }

    fun setAlarmTitle(alarmTitle: String?, alarmId: Int) {
        databaseExecutor.execute(Runnable { alarmDao.setAlarmTitle(alarmTitle, alarmId) })
    }

    fun reEnableAlarmChild(newParentAlarmId: Int) {
        databaseExecutor.execute(Runnable {
            try {
                databaseExecutor.awaitTermination(1, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val currentEntity = getAlarm(newParentAlarmId) ?: return@Runnable
            val ah = AlarmHelper()
            val daysOfRepeatArr = currentEntity.daysOfRepeatArr
            if (daysOfRepeatArr[Constants.IsRECURRING]) {
                for (i in 1 until daysOfRepeatArr.size) {
                    if (daysOfRepeatArr[i]) {
                        ah.repeatingAlarm(currentEntity, i)
                    }
                }
            }
        })
    }
}