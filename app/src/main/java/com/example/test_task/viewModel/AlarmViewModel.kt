package com.example.test_task.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.test_task.repository.AlarmRepository
import androidx.lifecycle.LiveData
import com.example.test_task.alarm.db.AlarmEntity

class AlarmViewModel(application : Application) : ViewModel() {
    private val alarmRepository: AlarmRepository

    init {
        alarmRepository = AlarmRepository(application)
    }

    val allAlarms: LiveData<List<AlarmEntity>>
        get() = alarmRepository.allAlarms
}