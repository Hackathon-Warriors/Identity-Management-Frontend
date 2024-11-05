package com.thales.idverification.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NetworkErrorDataModel(
	val errorReason: String? = null,
	val errorCode: String? = null
): Parcelable

