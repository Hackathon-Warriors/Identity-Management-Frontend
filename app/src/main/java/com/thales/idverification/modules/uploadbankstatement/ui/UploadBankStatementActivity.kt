package com.thales.idverification.modules.uploadbankstatement.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivityUploadBankStatementBinding
import com.thales.idverification.modules.success.ui.SuccessActivity
import com.thales.idverification.modules.uploadbankstatement.viewmodel.UploadBankStatementViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.FileUtil
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

@AndroidEntryPoint
class UploadBankStatementActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityUploadBankStatementBinding

    private lateinit var selectedBankStatementUri: Uri

    private val uploadBankStatementViewModel: UploadBankStatementViewModel by viewModels()

    // Registering for activity result to handle the selected PDF
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                selectedBankStatementUri = uri
                handleSelectedPdf(uri)
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUploadBankStatementBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {
            itemImg.setOnClickListener {
                // Trigger the PDF picker
                pickPdfDocument()
            }
            createItem.setOnClickListener {
                verifyPdfDocument()
            }
        }
    }
    
    private fun verifyPdfDocument() {
        
        ProgressBarUtil.showProgressBar(
            viewBinding.uploadBankStatementLoadingPanel.loadingPanel,
            viewBinding.createItem
        )

        val checkBankStatementBody = FileUtil.createMultipartBody(
            this@UploadBankStatementActivity,
            selectedBankStatementUri,
            1,
            UUID.randomUUID(),
            "statement_doc",
            filePassword = "password",
            fileType = "file"
        )

        uploadBankStatementViewModel.checkBankStatement(
            checkBankStatementBody
        ).observe(this@UploadBankStatementActivity) {
            ProgressBarUtil.hideProgressBar(
                viewBinding.uploadBankStatementLoadingPanel.loadingPanel,
                viewBinding.createItem
            )

            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data != null) {
                        if(it.data.error_msg != "") {
                            showErrorMessage(it.data.error_msg)
                        } else {
                            showSuccessMessage("Bank Statement verified successfully!")
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
    
    private fun pickPdfDocument() {
        // Set up the intent to choose PDF documents
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Create a chooser for the intent
        val chooser = Intent.createChooser(intent, "Select PDF Document")

        // Launch the chooser
        openDocumentLauncher.launch(chooser)
    }

    private fun handleSelectedPdf(uri: Uri) {
        // Display file path in the TextView
        val file = copyPdfToInternalStorage(uri)
        if (file != null) {
            viewBinding.itemImg.text = file.absolutePath
            showPdfPreview(file)
        } else {
            Toast.makeText(this, "Failed to access PDF file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyPdfToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "selected_pdf.pdf")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showPdfPreview(file: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val page = pdfRenderer.openPage(0)

            // Render the first page of the PDF as a bitmap
            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Display the bitmap in the ImageView
            viewBinding.img.setImageBitmap(bitmap)

            // Close resources
            page.close()
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to preview PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorMessage(errorDescription: String = getString(R.string.generic_error_description)) {
        DialogUtil.showCustomDialog(
            this@UploadBankStatementActivity,
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.generic_error_title),
            "$errorDescription\nPlease try again",
            getString(R.string.generic_error_button_text)
        )
    }
    private fun showSuccessMessage(message: String) {
        DialogUtil.showCustomDialog(
            this@UploadBankStatementActivity,
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            "Success",
            message,
            "Continue"
        ) {
            startActivity(Intent(this@UploadBankStatementActivity, SuccessActivity::class.java))
            this@UploadBankStatementActivity.finish()
        }
    }

}