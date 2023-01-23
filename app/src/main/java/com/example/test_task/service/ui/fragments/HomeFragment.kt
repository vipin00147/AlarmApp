package com.example.test_task.service.ui.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.test_task.R
import com.example.test_task.adapter.AlarmRecViewAdapter
import com.example.test_task.alarm.AlarmHelper
import com.example.test_task.alarm.NotificationHelper
import com.example.test_task.alarm.db.AlarmEntity
import com.example.test_task.base.BaseFragment
import com.example.test_task.databinding.FragmentHomeBinding
import com.example.test_task.utils.changeFragment
import com.example.test_task.viewModel.AlarmViewModel
import com.example.test_task.viewModel.ViewModelFactory
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private var alarmViewModel: AlarmViewModel ?= null
    private var alarmHelper: AlarmHelper? = null
    private val listOfAlarms = ArrayList<AlarmEntity>()
    private val mAdapter = AlarmRecViewAdapter(this, listOfAlarms)

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmHelper = AlarmHelper()

        initRecyclerView()
        initViewModel()
        alarmListObserver()

        binding?.materialToolbar?.setOnMenuItemClickListener {
            AddNewAlarmFragment().changeFragment(R.id.mainContainer, getBaseActivity(), true)
            return@setOnMenuItemClickListener true
        }

        val notificationHelper = NotificationHelper(getBaseActivity(), -1)
        notificationHelper.createNotificationChannel()

        PreferenceManager.setDefaultValues(getBaseActivity(), R.xml.preferences, false)

    }

    private fun initRecyclerView() {
        val mLayoutManager: LayoutManager = LinearLayoutManager(getBaseActivity())

        binding?.itemAlarmRecyclerView?.setHasFixedSize(true)
        binding?.itemAlarmRecyclerView?.layoutManager = mLayoutManager
        binding?.itemAlarmRecyclerView?.adapter = mAdapter
    }

    private fun initViewModel() {
        alarmViewModel = ViewModelProviders.of(this, ViewModelFactory(getBaseActivity().application))[AlarmViewModel::class.java]
    }

    private fun alarmListObserver() {

        alarmViewModel?.allAlarms?.observe(viewLifecycleOwner,
            Observer<List<Any?>> { alarmEntities ->

                listOfAlarms.clear()
                listOfAlarms.addAll(alarmEntities as ArrayList<AlarmEntity>)
                mAdapter.notifyDataSetChanged()

                if (alarmEntities.isEmpty()) {
                    binding?.imageWhenEmpty?.showView()
                    binding?.textWhenEmpty?.showView()
                } else {
                    binding?.imageWhenEmpty?.hideView()
                    binding?.textWhenEmpty?.hideView()
                }
            })
    }

    fun deleteAlarm(position: Int) {
        alarmHelper?.cancelAlarm(listOfAlarms[position], true, true, -1)
        listOfAlarms.removeAt(position)
        mAdapter.notifyItemRemoved(position)
    }

    fun setOnOffAlarm(position: Int, isChecked: Boolean) {
        when(isChecked) {
            true -> {
                alarmHelper?.cancelAlarm(listOfAlarms[position],false,true, -1)
                listOfAlarms[position].setmAlarmEnabled(false)
                mAdapter.notifyDataSetChanged()
            }
            false -> {
                alarmHelper?.oldAlarmId = listOfAlarms[position].alarmId
                alarmHelper?.reEnableAlarm(listOfAlarms[position])
                listOfAlarms[position].setmAlarmEnabled(true)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}