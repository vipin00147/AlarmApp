package com.example.test_task.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.test_task.R
import com.example.test_task.alarm.db.AlarmEntity
import com.example.test_task.databinding.AlarmItemBinding
import com.example.test_task.service.ui.fragments.HomeFragment
import java.text.SimpleDateFormat
import java.util.*

class AlarmRecViewAdapter(val fragment: HomeFragment, var productList: ArrayList<AlarmEntity>) : RecyclerView.Adapter<AlarmRecViewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder) {
            with(productList[position]) {

                val alarmTimeInMillis: Long = this.alarmTime
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = sdf.format(alarmTimeInMillis)
                binding.itemAlarmTime.text = formattedTime.toUpperCase()
                binding.itemAlarmEnabled.isChecked = this.alarmEnabled
                binding.itemAlarmDelete.setOnClickListener {
                    fragment.deleteAlarm(adapterPosition)
                }

                binding.itemAlarmEnabled.setOnCheckedChangeListener { compoundButton, boolean ->
                    fragment.setOnOffAlarm(adapterPosition, !boolean)
                }

                when(this.alarmEnabled) {
                    true -> {
                        binding.container.setBackgroundColor(fragment.resources.getColor(R.color.white))
                    }
                    false -> {
                        binding.container.setBackgroundColor(fragment.resources.getColor(R.color.off_alarm_bg))
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
