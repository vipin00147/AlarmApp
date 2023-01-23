package com.example.test_task.utils

import android.app.Application
import java.util.concurrent.Executors

open class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object myStaticObjects{
        private const val NUMBER_OF_THREADS = 4
        val databaseExecutor = Executors.newFixedThreadPool(
            NUMBER_OF_THREADS)
        var context: MyApplication? = null
            private set
    }
}