package com.example.aptemergency

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.aptemergency.data.model.Location
import com.example.aptemergency.data.model.Request
import com.example.aptemergency.utils.*
import com.example.aptemergency.utils.Utils.observeOnce
import com.example.aptemergency.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val storageRequestCode = 1002
    private val LOCATION_REQUEST_CODE = 123
    private val GPS_REQUEST_CHECK_SETTINGS = 102
    private val SNAPSHOT_REQUEST_CODE = 101
    private var bitmap: Bitmap? = null
    private val requiredLocationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val perms = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var isGPSEnabled = false
    private var locationModel: LocationLiveData.LocationModel? = null
    private var photoTaken: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GpsUtil(this).turnGPSOn(object : GpsUtil.OnGpsListener {
            override fun gpsStatus(isGPSEnabled: Boolean) {
                this@MainActivity.isGPSEnabled = isGPSEnabled
            }
        })

        val camera = findViewById<CameraView>(R.id.camera)
        camera.setLifecycleOwner(this)

        setUpObserver()
        setSosNumbers()
        takePicture.setOnClickListener {
            if (checkStoragePermission()) {
                if (camera.isVisible) {
                    camera.takePicture()
                    invokeLocationAction()
                }
            } else {
                requestCameraAndFileWritePermissions()
            }
        }

        removePicture.setOnClickListener {
            removePicture()
        }

        sendRequest.setOnClickListener {
            takePhotoAndSendEmergency()
        }


        camera.addCameraListener(object : CameraListener() {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPictureTaken(result: PictureResult) {
                // Access the raw data if needed.

                photoTaken = result.data.toBase64()
                bitmap = BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
                Glide.with(this@MainActivity).load(bitmap).into(preview)
                camera.visibility = View.GONE
                preview.visibility = View.VISIBLE
            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
                Toast.makeText(
                    this@MainActivity,
                    exception.message!!,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setSosNumbers() {
        val firstNumber = sharedPreferences.getString(Constants.PREF_PHONE_ONE, "")
        val secondNumber = sharedPreferences.getString(Constants.PREF_PHONE_TWO, "")
        emergency_number_one.editText?.setText(firstNumber)
        emergency_number_two.editText?.setText(secondNumber)
    }


    private fun takePhotoAndSendEmergency() {
        try {
            if (bitmap == null) throw ValidationException("please take a picture")
            val photo = photoTaken
            val phone1 =
                emergency_number_one.validateWithValueOrThrow(" first emergency number")
            val phone2 =
                emergency_number_two.validateWithValueOrThrow("second emergency number")
            sharedPreferences.edit().putString(Constants.PREF_PHONE_ONE, phone1).apply()
            sharedPreferences.edit().putString(Constants.PREF_PHONE_TWO, phone2).apply()
            val longitude = locationModel?.longitude.toString()
            val latitude = locationModel?.latitude.toString()
            val numbers = listOf(phone1, phone2)
            val request = Request(numbers, photo!!, Location(longitude, latitude))
            viewModel.sendEmergency(request)

        } catch (e: ValidationException) {
            Toast.makeText(this, e.message!!, Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpObserver() {
        viewModel.sendEmergency.observe(this, Observer { response ->
            response.getContentIfNotHandled()?.let {

                when (it.status) {
                    Resource.Status.LOADING -> {
                        progress.visibility = View.VISIBLE
                        !takePicture.isClickable
                        !removePicture.isClickable
                    }
                    Resource.Status.SUCCESS -> {
                        Log.e("image", it.data.toString())
                        Toast.makeText(this, it.data, Toast.LENGTH_SHORT)
                            .show()
                        takePicture.isClickable
                        camera.visibility = View.VISIBLE
                        preview.visibility = View.GONE
                        progress.visibility = View.GONE
                    }
                    Resource.Status.ERROR -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this, it.data!!, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun removePicture() {
        if (bitmap != null) {
            bitmap = null
            camera.visibility = View.VISIBLE
            preview.visibility = View.GONE
        } else {
            Toast.makeText(
                this,
                "Please take a picture first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ByteArray.toBase64(): String =
        String(Base64.getEncoder().encode(this))

    private fun checkStoragePermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.CAMERA
        )

        val writeFilePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return cameraPermission == PackageManager.PERMISSION_GRANTED
                    || writeFilePermission == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun requestCameraAndFileWritePermissions() {
        ActivityCompat.requestPermissions(
            this,
            perms,
            SNAPSHOT_REQUEST_CODE
        )
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
        when (requestCode) {
            storageRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // clearImageOrLaunchImageCaptureIntent()
                } else {
                    Toast.makeText(this, "Permission is required", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun locationPermissionGranted() = requiredLocationPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestLocationPermissionRationale() = requiredLocationPermissions.all {
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
                        requestPermissions(requiredLocationPermissions, LOCATION_REQUEST_CODE)
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
                requestPermissions(requiredLocationPermissions, LOCATION_REQUEST_CODE)
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
                }
            }
        }
    }


    @Throws(ValidationException::class)
    fun TextInputLayout.validateWithValueOrThrow(
        labelName: String,
    ): String {

        var value = this.editText?.text.toString()

        //Validate that it contain something
        if (value.trim().isBlank()) {
            this.error = "$labelName is required"
            this.isErrorEnabled = true
            throw  ValidationException("$labelName is required")
        }

        this.error = " "
        this.isErrorEnabled = false
        return value.trim()
    }
}
