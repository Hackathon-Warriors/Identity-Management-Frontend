package com.thales.idverification.modules.uploadimage.ui

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivityUploadImageBinding
import com.thales.idverification.modules.uploadbankstatement.ui.UploadBankStatementActivity
import com.thales.idverification.modules.uploadimage.viewmodel.UploadImageViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.FileUtil.createMultipartBody
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.UUID

@AndroidEntryPoint
class UploadImageActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityUploadImageBinding

    private lateinit var selectedImage: Uri
    private var part_image: String? = null

    private val uploadImageViewModel: UploadImageViewModel by viewModels()

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
                    }
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUploadImageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {
            itemImg.setOnClickListener {
                pick(it)
            }
            createItem.setOnClickListener {
                verifyIdentityDocument()
            }
        }
    }

    private fun verifyIdentityDocument() {

        ProgressBarUtil.showProgressBar(
            viewBinding.uploadImageLoadingPanel.loadingPanel,
            viewBinding.createItem
        )

        val verifyIdentityDocumentBody = createMultipartBody(
            this@UploadImageActivity,
            selectedImage,
            1,
            UUID.randomUUID(),
            "poi_doc",
            fileType = "file"
        )

        uploadImageViewModel.verifyIdentityDocument(
            verifyIdentityDocumentBody
        ).observe(this@UploadImageActivity) {
            ProgressBarUtil.hideProgressBar(
                viewBinding.uploadImageLoadingPanel.loadingPanel,
                viewBinding.createItem
            )

            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        if(it.errorData.errorReason != null) {
                            showErrorMessage(it.errorData.errorReason)
                        } else {
                            showErrorMessage()
                        }
                    } else {
                        showSuccessMessage("Document verified successfully!")
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {
                    showErrorMessage()
                }
            }

        }
    }

    // Method for starting the activity for selecting image from phone storage
    private fun pick(view: View?) {
        verifyStoragePermissions(this)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        val galleryIntent = Intent.createChooser(intent, "Open Gallery")
        galleryIntent.putExtra(REQUEST_CODE_KEY, PICK_IMAGE_REQUEST)

        resultLauncher.launch(Intent.createChooser(intent, "Open Gallery"))
    }

    private fun verifyStoragePermissions(activity: Activity?) {
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

    private fun showErrorMessage(errorDescription: String = getString(R.string.generic_error_description)) {
        DialogUtil.showCustomDialog(
            this@UploadImageActivity,
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.generic_error_title),
            "$errorDescription\nPlease try again",
            getString(R.string.generic_error_button_text)
        )
    }
    private fun showSuccessMessage(message: String) {
        DialogUtil.showCustomDialog(
            this@UploadImageActivity,
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            "Success",
            message,
            "Continue"
        ) {
            startActivity(Intent(this@UploadImageActivity, UploadBankStatementActivity::class.java))
            this@UploadImageActivity.finish()
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