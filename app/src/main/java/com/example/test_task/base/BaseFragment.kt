package com.example.test_task.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.test_task.R
import com.example.test_task.service.ui.activities.MainActivity
import java.io.*
import java.util.*

abstract class BaseFragment<V : ViewBinding> : Fragment() {

    protected var binding: V? = null
    private var mContext : Context ?= null
    lateinit var baseActivity : BaseActivity<ViewBinding>
    lateinit var mainActivity : MainActivity<ViewBinding>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        if(binding?.root == null) {
            binding = onCreateBinding(inflater, container, savedInstanceState)
        }

        return binding?.root
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    abstract fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): V

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        try {
            baseActivity = context as BaseActivity<ViewBinding>

            when(mContext) {
                is MainActivity<*> -> {
                    mainActivity = context as MainActivity<ViewBinding>
                }
            }
        }
        catch (ex : Exception){}
    }

    @JvmName("getBaseActivity1")
    fun getBaseActivity() : BaseActivity<ViewBinding> {
        return baseActivity
    }

    @JvmName("getHomeActivity1")
    fun getMainActivity(): MainActivity<ViewBinding> {
        return mainActivity
    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun View.showView() {
        this.visibility = View.VISIBLE
    }

    fun hideKeyboard() {
        getBaseActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun View.hideImageWithAnim() {

        this.showView()
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        this.startAnimation(animation)
        Handler().postDelayed({
            this.hideView()
        }, 1000)
    }
}