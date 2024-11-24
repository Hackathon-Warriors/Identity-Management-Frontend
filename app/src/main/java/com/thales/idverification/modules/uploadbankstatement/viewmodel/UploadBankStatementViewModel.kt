package com.thales.idverification.modules.uploadbankstatement.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.CheckBankStatementResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class UploadBankStatementViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun checkBankStatement(
        @Body checkBankStatementRequest: RequestBody
    ): LiveData<WOTRCaller<CheckBankStatementResponse>> {
        return repository.checkBankStatement(checkBankStatementRequest)
    }
}
