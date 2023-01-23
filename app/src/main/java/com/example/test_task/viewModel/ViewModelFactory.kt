package com.example.test_task.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(application) as T
        }
        else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}