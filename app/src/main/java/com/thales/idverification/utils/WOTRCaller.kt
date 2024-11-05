package com.thales.idverification.utils

import com.thales.idverification.network.NetworkErrorDataModel

data class WOTRCaller<out T>(val status: Status, val data: T?, val message: String?, val errorData: NetworkErrorDataModel?){
    companion object{
        fun <T> success(data: T?, errorData: NetworkErrorDataModel?) = WOTRCaller(Status.SUCCESS, data, null, errorData)
        fun <T> progress(data: T?, errorData: NetworkErrorDataModel?) = WOTRCaller(Status.PROGRESS, data, null, errorData)
        fun <T> fail(data: T?, message: String?, errorData: NetworkErrorDataModel?) = WOTRCaller(
            Status.FAIL, data, message, errorData)
    }
}
