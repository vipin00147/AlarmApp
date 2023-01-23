package com.example.test_task.alarm

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.test_task.R
import com.example.test_task.alarm.db.AlarmEntity
import com.example.test_task.repository.AlarmRepository
import com.example.test_task.service.AlarmService
import com.example.test_task.utils.Constants
import com.example.test_task.utils.MyApplication
import java.util.*

class AlarmHelper {

    var oldAlarmId = 0
    private var context: Context? = null
    private var app: Application? = null
    private var ar: AlarmRepository? = null

    private var isNew = true

    fun cancelAlarm(
        alarmEntity: AlarmEntity, delete: Boolean,
        cancelParent: Boolean, dayOfRepeat: Int,
    ) {

        var alarmEntity: AlarmEntity = alarmEntity
        context = MyApplication.context
        app = Application()
        ar = AlarmRepository(app)

        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val daysOfRepeatArr: Array<Boolean> = alarmEntity.daysOfRepeatArr

        if (cancelParent) {

            val parentAlarmId: Int = alarmEntity.alarmId

            alarmManager.cancel(getPendingIntent(parentAlarmId))

            ar?.updateAlarmStatus(parentAlarmId, false)

            if (delete) ar?.deleteAlarm(parentAlarmId)

            if (daysOfRepeatArr[Constants.IsRECURRING]) {
                var childAlarmId: Int
                for (i in 1 until daysOfRepeatArr.size) {
                    if (daysOfRepeatArr[i]) {
                        Log.i(TAG, "cancelAlarm: Child Alarm was Enabled$i")
                        childAlarmId = parentAlarmId + i
                        alarmManager.cancel(getPendingIntent(childAlarmId))
                    }
                }
            }
        } else {

            val childAlarmId: Int = alarmEntity.alarmId + dayOfRepeat
            alarmManager.cancel(getPendingIntent(childAlarmId))

            daysOfRepeatArr[dayOfRepeat] = false
            alarmEntity = AlarmEntity(alarmEntity.alarmTime, alarmEntity.alarmId,
                alarmEntity.alarmEnabled, daysOfRepeatArr, alarmEntity.alarmTitle)
            ar?.update(alarmEntity)


            var flag = false
            for (i in 1 until daysOfRepeatArr.size) {

                if (daysOfRepeatArr[i]) {
                    flag = true
                    break
                }
            }

            if (!flag) daysOfRepeatArr[Constants.IsRECURRING] = false

            if (!flag && alarmEntity.alarmTime < System.currentTimeMillis()) {

                daysOfRepeatArr[Constants.IsRECURRING] = false
                ar?.updateAlarmStatus(alarmEntity.alarmId, false)
            }
        }
    }

    fun createAlarm(c: Calendar, alamTitle : String) {
        context = MyApplication.context
        app = Application()
        ar = AlarmRepository(app)
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmId = Random().nextInt(Int.MAX_VALUE)

        if (c.before(Calendar.getInstance())) {
            c[Calendar.DATE] = Calendar.getInstance()[Calendar.DATE]
            if (System.currentTimeMillis() > c.timeInMillis) c.add(Calendar.DATE, 1)
        }
        val alarmClockInfo = AlarmClockInfo(c.timeInMillis, null)
        alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(alarmId))

        val alarmTime = c.timeInMillis

        if (!isNew) {

            ar?.updateAlarmIdTime(oldAlarmId, alarmId, alarmTime)
        } else {
            val daysOfRepeatArr = arrayOfNulls<Boolean>(8)

            Arrays.fill(daysOfRepeatArr, false)
            val alarm = AlarmEntity(alarmTime, alarmId, true,
                daysOfRepeatArr, alamTitle)
            ar?.insert(alarm)
        }
    }

    fun repeatingAlarm(alarmEntity: AlarmEntity, dayOfRepeat: Int) {
        var alarmEntity: AlarmEntity = alarmEntity
        context = MyApplication.context
        app = Application()
        ar = AlarmRepository(app)

        val daysOfRepeatArr: Array<Boolean> = alarmEntity.daysOfRepeatArr
        daysOfRepeatArr[Constants.IsRECURRING] = true
        daysOfRepeatArr[dayOfRepeat] = true

      if (!alarmEntity.alarmEnabled) {

            alarmEntity = AlarmEntity(alarmEntity.alarmTime, alarmEntity.alarmId,
                alarmEntity.alarmEnabled, daysOfRepeatArr, alarmEntity.alarmTitle)
            ar?.update(alarmEntity)
            return
        }

        val parentAlarmId: Int = alarmEntity.alarmId
        val childAlarmId = parentAlarmId + dayOfRepeat
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val cal = Calendar.getInstance()

        cal.timeInMillis = alarmEntity.alarmTime

        val parentTime = Calendar.getInstance()
        parentTime.timeInMillis = alarmEntity.alarmTime
        val parentAlarmDay = parentTime[Calendar.DAY_OF_WEEK]
        if (dayOfRepeat == parentAlarmDay ||
            dayOfRepeat == Calendar.getInstance()[Calendar.DAY_OF_WEEK]
        ) cal.add(Calendar.WEEK_OF_MONTH, 1)


        cal[Calendar.DAY_OF_WEEK] = dayOfRepeat

         if (cal.before(Calendar.getInstance())) cal.add(Calendar.WEEK_OF_MONTH, 1)

        cal[Calendar.DAY_OF_WEEK] = dayOfRepeat


        val alarmClockInfo = AlarmClockInfo(cal.timeInMillis, null)
        alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(childAlarmId))

        alarmEntity = AlarmEntity(alarmEntity.alarmTime, parentAlarmId,
            alarmEntity.alarmEnabled, daysOfRepeatArr, alarmEntity.alarmTitle)
        ar?.update(alarmEntity)
    }

    fun reEnableAlarm(alarmEntity: AlarmEntity) {
        if (oldAlarmId == 0) Log.e(TAG, "reEnableAlarm: oldAlarmId NOT SET !")
        val cal = Calendar.getInstance()
        cal.timeInMillis = alarmEntity.alarmTime
        isNew = false
       createAlarm(cal, alarmEntity.alarmTitle)
    }

    private fun getPendingIntent(alarmId: Int): PendingIntent {
        context = MyApplication.context
        val intent = Intent(context, AlarmService::class.java)

        intent.putExtra("alarmIdKey", alarmId)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getForegroundService(context, alarmId, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getForegroundService(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(context, alarmId, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    companion object {
        private const val TAG = "AlarmHelper"
    }
}
