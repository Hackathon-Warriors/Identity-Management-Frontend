package com.thales.idverification.modules.customviews.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationItemView(
    val image: Int?,
    val activityName: String?,
    val address: String?,
    val activityDate: String?,
    var entityName: String? = null,
    var description: String? = null,
    var latitude: String? = null,
    var longitude: String? = null
): Parcelable
