package com.example.test_task.utils


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Fragment.changeFragment(containerId: Int, requireActivity: FragmentActivity, isAddToBakStack : Boolean) {

    var fragment = this
    val transaction = requireActivity.supportFragmentManager.beginTransaction()
    transaction.let {
        it.replace(containerId, fragment)
        if(isAddToBakStack) {
            it.addToBackStack(this.javaClass.canonicalName)
        }
        it.commit()
    }
}