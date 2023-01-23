package com.example.test_task.service.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.test_task.R
import com.example.test_task.alarm.AlarmHelper
import com.example.test_task.base.BaseFragment
import com.example.test_task.databinding.FragmentAddNewAlarmBinding
import com.example.test_task.utils.TimeFormatUtils
import java.util.*

class AddNewAlarmFragment : BaseFragment<FragmentAddNewAlarmBinding>(), View.OnClickListener {

    private lateinit var alarmTime : Calendar
    var isTimeSet = false


    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): FragmentAddNewAlarmBinding {
        return FragmentAddNewAlarmBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.materialToolbar?.setNavigationOnClickListener {
            getBaseActivity().supportFragmentManager.popBackStack()
        }

        binding?.itemAlarmTime?.setOnClickListener(this)
        binding?.amPmLayout?.setOnClickListener(this)
        binding?.saveBtn?.setOnClickListener(this)
    }

    private fun openTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                isTimeSet = true
                alarmTime = Calendar.getInstance()
                alarmTime[Calendar.HOUR_OF_DAY] = hourOfDay
                alarmTime[Calendar.MINUTE] = minute
                alarmTime[Calendar.SECOND] = 0

                val AM_PM = alarmTime.get(Calendar.AM_PM)
                when(AM_PM) {
                    Calendar.AM -> {
                        binding?.itemAlarmTime?.text = "${if(hourOfDay ==  0) 12 else hourOfDay}:$minute"
                        binding?.amText?.setTextColor(resources.getColor(R.color.black))
                        binding?.pmText?.setTextColor(resources.getColor(R.color.unselected_ap_pm_color))
                    }
                    Calendar.PM -> {
                        binding?.itemAlarmTime?.text = "${if(hourOfDay ==  12) hourOfDay else hourOfDay-12}:$minute"
                        binding?.amText?.setTextColor(resources.getColor(R.color.unselected_ap_pm_color))
                        binding?.pmText?.setTextColor(resources.getColor(R.color.black))
                    }
                }

            }
        val currentTime = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(getBaseActivity(),
            timeSetListener,
            currentTime[Calendar.HOUR_OF_DAY],
            currentTime[Calendar.MINUTE],
            DateFormat.is24HourFormat(getBaseActivity()))
        timePickerDialog.show()
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            binding?.amPmLayout?.id, binding?.itemAlarmTime?.id -> {
                openTimePicker()
            }
            binding?.saveBtn?.id -> {

                when(isTimeSet) {
                    false -> {
                        getBaseActivity().showHomeErrorSnackBar("Error : Please Set Alarm Time")
                    }
                    true -> {

                        AlarmHelper().createAlarm(alarmTime, binding?.alarmName?.text.toString().trim())

                        getBaseActivity().showHomeErrorSnackBar(getString(R.string.alarm_set_for) + " "
                                + TimeFormatUtils.getFormattedNextAlarmTime(alarmTime.timeInMillis))

                        getBaseActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }
}