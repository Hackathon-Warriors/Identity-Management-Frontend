package com.thales.idverification.modules.imagepreview.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.CheckLiveLinessResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class ImagePreviewViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun checkLiveliness(
        @Body checkLivelinessRequest: RequestBody
    ): LiveData<WOTRCaller<CheckLiveLinessResponse>> {
        return repository.checkLiveliness(checkLivelinessRequest)
    }
}
