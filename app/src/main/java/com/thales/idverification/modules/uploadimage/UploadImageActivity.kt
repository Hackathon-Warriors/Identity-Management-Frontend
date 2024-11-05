package com.thales.idverification.modules.uploadimage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import com.thales.idverification.databinding.ActivityUploadImageBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class UploadImageActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityUploadImageBinding

    private lateinit var selectedImage: Uri
    private var part_image: String? = null


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
//                val requestCode = data?.extras?.getInt(REQUEST_CODE_KEY)

                data?.data?.let {
                    selectedImage = it // Get the image file URI

                    val imageProjection = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor? =
                        contentResolver.query(selectedImage, imageProjection, null, null, null)
                    if (cursor != null) {

                        cursor.moveToFirst()

                        val indexImage: Int = cursor.getColumnIndex(imageProjection[0])
                        part_image = cursor.getString(indexImage)
                        viewBinding.itemImg.text = part_image // Get the image file absolute path

                        var bitmap: Bitmap? = null
                        try {
                            bitmap = if(Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(
                                    this.contentResolver,
                                    selectedImage
                                )
                            } else {
                                val source = ImageDecoder.createSource(contentResolver, selectedImage)
                                ImageDecoder.decodeBitmap(source)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        viewBinding.img.setImageBitmap(bitmap) // Set the ImageView with the bitmap of the image
                        cursor.close()

                        populateFieldsFromImage()
                    }
                }
            }

        }

    private fun populateFieldsFromImage() {

        val exifInterface =
            contentResolver.openFileDescriptor(selectedImage, "r")?.fileDescriptor
                ?.let { inputStream -> ExifInterface(inputStream) }

        val gpsCoordinates = convertGpsCoordinates(
            exifInterface?.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
            exifInterface?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF),
            exifInterface?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
            exifInterface?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        )

        viewBinding.apply {
            latitude.apply {
                value.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                value.setText(gpsCoordinates[0])
            }
            longitude.apply {
                value.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                value.setText(gpsCoordinates[1])
            }
            activityDate.apply {
                value.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                value.setText(exifInterface?.getAttribute(ExifInterface.TAG_GPS_DATESTAMP))
            }
        }

    }

    private fun convertGpsCoordinates(
        latitude: String?,
        latitudeRef: String?,
        longitude: String?,
        longitudeRef: String?
    ): Array<String> {
        if ((latitude!=null) && (latitudeRef!=null) && (longitude!=null) && (longitudeRef!=null)) {

            val convertedLatitude = if (latitudeRef == "N") {
                convertToDegree(latitude)
            } else {
                0 - convertToDegree(latitude)
            }

            val convertedLongitude = if (longitudeRef == "E") {
                convertToDegree(longitude)
            } else {
                0 - convertToDegree(longitude)
            }

            return arrayOf(convertedLatitude.toString(), convertedLongitude.toString())

        } else {
            return arrayOf("", "")
        }
    }

    private fun convertToDegree(stringDMS: String): Float {

        var result: Float

        val DMS = stringDMS.split(",".toRegex(), 3).toTypedArray()
        val stringD = DMS[0].split("/".toRegex(), 2).toTypedArray()

        val D0 = stringD[0].toDouble()
        val D1 = stringD[1].toDouble()

        val floatD = D0 / D1

        val stringM = DMS[1].split("/".toRegex(), 2).toTypedArray()

        val M0: Double = stringM[0].toDouble()
        val M1: Double = stringM[1].toDouble()

        val floatM = M0 / M1

        val stringS = DMS[2].split("/".toRegex(), 2).toTypedArray()
        val S0: Double = stringS[0].toDouble()
        val S1: Double = stringS[1].toDouble()

        val floatS = S0 / S1

        result = (floatD + floatM / 60 + floatS / 3600).toFloat()
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUploadImageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.itemImg.setOnClickListener {
            pick(it)
        }

        viewBinding.apply {
            latitude.field.text = "Latitude"
            longitude.field.text = "Longitude"
            address.field.text = "Address"
            activityName.field.text = "Activity Name"
            activityDate.field.text = "Activity Date"
            entityName.field.text = "Entity Name"
            entityType.field.text = "Entity Type"
            description.field.text = "Description"
        }
    }

    // Method for starting the activity for selecting image from phone storage
    fun pick(view: View?) {
        verifyStoragePermissions(this)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        val galleryIntent = Intent.createChooser(intent, "Open Gallery")
        galleryIntent.putExtra(REQUEST_CODE_KEY, PICK_IMAGE_REQUEST)

        resultLauncher.launch(Intent.createChooser(intent, "Open Gallery"))
    }

    fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_KEY = "requestCode"
        private const val PICK_IMAGE_REQUEST = 9544
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}