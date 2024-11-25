package com.thales.idverification.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

object FileUtil {

    fun createMultipartBody(
        context: Context,
        fileUri: Uri?,
        userId: Int,
        requestId: UUID,
        fileKey: String,
        filePassword: String? = null,
        fileType: String = "file"
    ): RequestBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Add userId and requestId as form fields
        builder.addFormDataPart("user_id", userId.toString())
        builder.addFormDataPart("request_id", requestId.toString())

        val mediaType = if(fileType == "image") "image/*" else "application/octet-stream"

        // Add file
        fileUri?.let {
            val imageFile = uriToFile(context, it)
            val imageRequestBody = imageFile.asRequestBody(mediaType.toMediaTypeOrNull())
            builder.addFormDataPart(fileKey, imageFile.name, imageRequestBody)
        }

        filePassword?.let {
            builder.addFormDataPart("password", filePassword)
        }

        // Add generic file part
//        fileUri?.let {
//            val genericFile = uriToFile(context, it)
//            val fileRequestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), genericFile)
//            builder.addFormDataPart("file", genericFile.name, fileRequestBody)
//        }

        return builder.build()
    }

    // Helper to convert URI to File
    fun uriToFile(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }

    // Helper to get file name from URI
    fun getFileName(context: Context, uri: Uri): String {
        var name = "temp_file" // Default name if no valid name is found
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }
}