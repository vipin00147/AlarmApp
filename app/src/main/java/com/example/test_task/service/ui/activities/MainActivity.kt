package com.example.test_task.service.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.example.test_task.R
import com.example.test_task.base.BaseActivity
import com.example.test_task.databinding.ActivityMainBinding
import com.example.test_task.service.ui.fragments.HomeFragment
import com.example.test_task.utils.changeFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity<T> : BaseActivity<ActivityMainBinding>() {

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initHomeActivity(this as MainActivity<ViewBinding>)
        changeTopBarColor(resources.getColor(R.color.menu_bg_color))
        changeStatusBarIconColorToBlack(binding.root)

        requestNotificationPermissions()
    }

    private fun requestNotificationPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {

                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            HomeFragment().changeFragment(R.id.mainContainer, this@MainActivity, false)
                        }

                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                            showHomeErrorSnackBar("Open Setting")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        list: List<PermissionRequest?>?,
                        permissionToken: PermissionToken,
                    ) {
                        permissionToken.continuePermissionRequest()
                    }
                }).withErrorListener { error: DexterError? ->
                    Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
                }
                .onSameThread().check()
        }
        else {
            HomeFragment().changeFragment(R.id.mainContainer, this@MainActivity, false)
        }
    }
}