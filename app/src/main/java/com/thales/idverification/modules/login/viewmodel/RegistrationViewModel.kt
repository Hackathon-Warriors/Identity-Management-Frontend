package com.thales.idverification.modules.login.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.RegisterUserResponse
import com.thales.idverification.network.SendOTPResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun sendOTP(
        mobileNumber: String,
        isUserRegistered: Boolean
    ): LiveData<WOTRCaller<SendOTPResponse>> {
        return repository.sendOTP(mobileNumber, isUserRegistered)
    }

    fun registerUser(
        emailAddress: String,
        mobileNumber: String,
        isUserRegistered: Boolean,
        fullName: String,
        otp: String
    ): LiveData<WOTRCaller<RegisterUserResponse>> {
        return repository.registerUser(emailAddress, mobileNumber, isUserRegistered, fullName, otp)
    }
}