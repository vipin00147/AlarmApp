package com.example.test_task.utils

import android.content.Context
import com.example.test_task.R

object TimeFormatUtils {
    fun getFormattedNextAlarmTime(time: Long): String {
        val context: Context = MyApplication.context!!
        val mills = time - System.currentTimeMillis()
        val hour = mills.toInt() / (1000 * 60 * 60)
        val min = (mills / (1000 * 60)).toInt() % 60
        if (hour == 0 && min < 1) return context.getString(R.string.less_than_a_minute_from_now) else if (hour == 0) return min.toString() + " " + context.getString(
            R.string.minutes_from_now)
        return (hour.toString() + " " + context.getString(R.string.hours_and) + " " + min
                + " " + context.getString(R.string.minutes_from_now))
    }
}