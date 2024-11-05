package com.thales.idverification.network

import androidx.lifecycle.LiveData
import com.thales.idverification.utils.WOTRCaller

interface IUserVerification {
    fun verifyUser(verifyUserRequest: VerifyUserRequest): LiveData<WOTRCaller<VerifyUserResponse>>
    fun sendOTP(sendOTPRequest: SendOTPRequest): LiveData<WOTRCaller<SendOTPResponse>>
    fun loginUser(loginUserRequest: LoginUserRequest): LiveData<WOTRCaller<LoginUserResponse>>
    fun registerUser(registerUserRequest: RegisterUserRequest): LiveData<WOTRCaller<RegisterUserResponse>>
}