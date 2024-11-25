package com.thales.idverification.modules.imagepreview.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivityImagePreviewBinding
import com.thales.idverification.modules.imagepreview.viewmodel.ImagePreviewViewModel
import com.thales.idverification.modules.uploadimage.ui.UploadImageActivity
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.FileUtil
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ImagePreviewActivity : AppCompatActivity() {
    
    private lateinit var viewBinding: ActivityImagePreviewBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private val imagePreviewViewModel: ImagePreviewViewModel by viewModels()

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var exifInterface: ExifInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableButton(viewBinding.imageCaptureButton, true)
        enableButton(viewBinding.videoCaptureButton, true)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name, image metadata, and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
//        val imageMetadata = getImageMetadata()
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture
            .OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    try {
                        output.savedUri?.let { uri ->
                            checkLiveliness(uri)
                        }
                        exifInterface = output.savedUri?.let { uri ->
                            contentResolver.openFileDescriptor(uri, "rw")?.fileDescriptor
                                ?.let { inputStream -> ExifInterface(inputStream) }
                        }
//                        getLocation()
                    } catch(ioe: IOException) {
                        ioe.printStackTrace()
                    }

                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun checkLiveliness(capturedImageUri: Uri) {

        ProgressBarUtil.showProgressBar(
            viewBinding.imagePreviewLoadingPanel.loadingPanel,
            viewBinding.imageCaptureButton
        )

        val checkLivelinessBody = FileUtil.createMultipartBody(
            this@ImagePreviewActivity,
            capturedImageUri,
            1,
            UUID.randomUUID(),
            "selfie_image",
            fileType = "image"
        )

        imagePreviewViewModel.checkLiveliness(
            checkLivelinessBody
        ).observe(this@ImagePreviewActivity) {
            ProgressBarUtil.hideProgressBar(
                viewBinding.imagePreviewLoadingPanel.loadingPanel,
                viewBinding.imageCaptureButton
            )

            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data != null) {
                        if(it.data.error_msg != "") {
                            showErrorMessage(it.data.error_msg)
                        } else {
                            showSuccessMessage("Image verified successfully!")
                        }
                    } else {
                        showErrorMessage()
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {
                    showErrorMessage()
                }
            }

        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun updateLocationInImage(location: Location) {
        exifInterface?.apply {
            setGpsInfo(location)
            saveAttributes()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { locationInfo: Location? ->
            if (locationInfo != null) {
                updateLocationInImage(locationInfo)
            }
        }
    }

    private fun showErrorMessage(errorDescription: String = getString(R.string.generic_error_description)) {
        DialogUtil.showCustomDialog(
            this@ImagePreviewActivity,
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.generic_error_title),
            "$errorDescription\nPlease try again",
            getString(R.string.generic_error_button_text)
        )
    }
    private fun showSuccessMessage(message: String) {
        DialogUtil.showCustomDialog(
            this@ImagePreviewActivity,
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            "Success",
            message,
            "Continue"
        ) {
            startActivity(Intent(this@ImagePreviewActivity, UploadImageActivity::class.java))
            this@ImagePreviewActivity.finish()
        }
    }

    private fun enableButton(button: Button, enable: Boolean) {
        button.isEnabled = enable
        button.isClickable = enable
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ImagePreviewActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        private const val LOCATION_REQUEST = 1000
        private const val GPS_REQUEST = 1001
    }
}