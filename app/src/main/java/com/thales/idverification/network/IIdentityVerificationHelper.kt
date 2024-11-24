package com.thales.idverification.network

import androidx.lifecycle.LiveData;

import com.thales.idverification.utils.WOTRCaller;
import okhttp3.RequestBody
import retrofit2.http.Body

interface IIdentityVerificationHelper {
    fun checkLiveliness(@Body checkLivelinessRequest: RequestBody): LiveData<WOTRCaller<CheckLiveLinessResponse>>

    fun verifyIdentityDocument(@Body verifyIdentityDocumentRequest: RequestBody): LiveData<WOTRCaller<VerifyIdentityDocumentResponse>>

    fun checkBankStatement(@Body checkBankStatementRequest: RequestBody): LiveData<WOTRCaller<CheckBankStatementResponse>>
}
