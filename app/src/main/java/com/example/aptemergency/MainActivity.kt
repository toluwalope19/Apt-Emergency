package com.example.aptemergency

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.aptemergency.utils.GpsUtil
import com.example.aptemergency.utils.LocationLiveData
import com.example.aptemergency.utils.Utils.observeOnce
import com.example.aptemergency.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val storageRequestCode = 1002
    private val LOCATION_REQUEST_CODE = 123
    private val GPS_REQUEST_CHECK_SETTINGS = 102
    private val REQUIRED_LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var isGPSEnabled = false
    private var locationModel: LocationLiveData.LocationModel? = null
    private var cameraBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GpsUtil(this).turnGPSOn(object : GpsUtil.OnGpsListener {
            override fun gpsStatus(isGPSEnabled: Boolean) {
                this@MainActivity.isGPSEnabled = isGPSEnabled
            }
        })

        tv.setOnClickListener {
            Toast.makeText(this, "(${locationModel?.latitude} ${locationModel?.longitude})", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE) {
            invokeLocationAction()
        }
//        when (requestCode) {
//            storageRequestCode -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    clearImageOrLaunchImageCaptureIntent()
//                } else {
//                    Toast.makeText(this, "Permission is required", Toast.LENGTH_LONG)
//                        .show()
//                }
//            }
//        }
    }

    private fun locationPermissionGranted() = REQUIRED_LOCATION_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestLocationPermissionRationale() = REQUIRED_LOCATION_PERMISSIONS.all {
        shouldShowRequestPermissionRationale(it)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun invokeLocationAction() {
        when {
            locationPermissionGranted() -> {
                with(viewModel) {
                    getLocation().observeOnce(this@MainActivity, Observer { location ->
                        locationModel = location
                    })
                }
            }

            shouldShowRequestLocationPermissionRationale() -> {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("We need access to your location to get accurate information about your store!")
                    .setNegativeButton(
                        "No"
                    ) { _, _ -> }
                    .setPositiveButton(
                        "Ask me"
                    ) { _, _ ->
                        requestPermissions(REQUIRED_LOCATION_PERMISSIONS, LOCATION_REQUEST_CODE)
                    }
                    .show()
            }

            !isGPSEnabled -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Turn on GPS to get accurate information about your store!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            else -> {
                requestPermissions(REQUIRED_LOCATION_PERMISSIONS, LOCATION_REQUEST_CODE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {

                    GPS_REQUEST_CHECK_SETTINGS -> {
                        isGPSEnabled = true
                        invokeLocationAction()
                    }

                    else -> {
//                        val fileUri = data?.data
//                        fileUri?.let {
//                            updateImage(it)
//                            cropSelectedImage(it)
                    }

                }

            }
        }
    }
}
