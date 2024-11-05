package com.thales.idverification.modules.login.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.LoginUserResponse
import com.thales.idverification.network.SendOTPResponse
import com.thales.idverification.network.VerifyUserResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun verifyUser(mobileNumber: String) : LiveData<WOTRCaller<VerifyUserResponse>>{
        return repository.verifyUser(mobileNumber)
    }

    fun sendOTP(
        mobileNumber: String,
        isUserRegistered: Boolean
    ): LiveData<WOTRCaller<SendOTPResponse>> {
        return repository.sendOTP(mobileNumber, isUserRegistered)
    }

    fun loginUser(
        mobileNumber: String,
        otp: String
    ): LiveData<WOTRCaller<LoginUserResponse>> {
        return repository.loginUser(mobileNumber, otp)
    }
}