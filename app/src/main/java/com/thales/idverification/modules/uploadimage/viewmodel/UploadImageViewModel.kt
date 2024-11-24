package com.thales.idverification.modules.uploadimage.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.VerifyIdentityDocumentResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class UploadImageViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun verifyIdentityDocument(
        @Body verifyIdentityDocumentRequest: RequestBody
    ): LiveData<WOTRCaller<VerifyIdentityDocumentResponse>> {
        return repository.verifyIdentityDocument(verifyIdentityDocumentRequest)
    }
}
