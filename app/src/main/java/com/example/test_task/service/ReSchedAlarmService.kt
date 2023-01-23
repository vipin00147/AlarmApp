package com.example.test_task.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.test_task.alarm.AlarmHelper
import com.example.test_task.alarm.db.AlarmEntity
import com.example.test_task.repository.AlarmRepository
import com.example.test_task.utils.Constants
import com.example.test_task.utils.MyApplication

class ReSchedAlarmService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        Log.i(TAG, "onHandleWork: Triggered")
        val ah = AlarmHelper()

        val ar =
            AlarmRepository(MyApplication.context)
        val alarms: List<AlarmEntity> = ar.allAlarmsReSched

        var ae: AlarmEntity
        for (i in alarms.indices) {
            ae = alarms[i]
            if (ae.alarmEnabled) {

                if (ae.alarmTime < System.currentTimeMillis() &&
                    !ae.daysOfRepeatArr.get(Constants.IsRECURRING)
                ) {
                    Log.e(TAG, "onHandleWork: ParentTime passed, no child alarms")
                    ah.cancelAlarm(ae, false, true, -1)
                    continue
                } else if (ae.alarmTime < System.currentTimeMillis()) {
                    Log.e(TAG, "onHandleWork: ParentTime passed, but child alarms enabled")
                    ar.reEnableAlarmChild(ae.alarmId)
                    continue
                }

                ah.oldAlarmId = ae.alarmId
                ah.reEnableAlarm(ae)
                Log.i(TAG, "onHandleWork: AlarmEnabled(OldId): " + ae.alarmId)
            }
        }
    }

    companion object {
        const val JOB_ID = 1
        private const val TAG = "ReSchedAlarmService"
        fun enqueueWork(context: Context?, jobServiceIntent: Intent?) {
            enqueueWork(context!!,
                ReSchedAlarmService::class.java, JOB_ID,
                jobServiceIntent!!)
        }
    }
}