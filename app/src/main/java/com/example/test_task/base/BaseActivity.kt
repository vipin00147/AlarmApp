package com.example.test_task.base

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.example.test_task.R
import com.example.test_task.service.ui.activities.MainActivity
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {

    var shake: Animation?= null
    protected lateinit var binding: V

    var orientation = 0
    var homeActivity : MainActivity<ViewBinding>?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = createBinding()
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    fun initHomeActivity(activity: MainActivity<ViewBinding>) {
        this.homeActivity = activity
    }

    abstract fun createBinding(): V

    fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    fun showCutoutsOnNotch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    fun changeTopBarColor(color : Int) {
        window.statusBarColor = color
    }

    fun changeStatusBarIconColorToWhite(view : View) {
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
    }

    fun changeStatusBarIconColorToBlack(view : View) {
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
    }

    fun showHomeErrorSnackBar(msg: String) {
        val snackbar = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.button_bg))
            .setTextColor(resources.getColor(R.color.white))

        val layout = snackbar.view
        layout.setBackgroundColor(resources.getColor(R.color.button_bg))
        val text = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView

        //setting font color
        text.setTextColor(resources.getColor(R.color.white))

        snackbar.show()

    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun View.showView() {
        this.visibility = View.VISIBLE
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN ) {
            val v = currentFocus
            if (v is AppCompatEditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}