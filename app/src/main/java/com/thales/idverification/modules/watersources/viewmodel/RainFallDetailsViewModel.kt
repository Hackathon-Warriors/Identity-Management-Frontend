package com.thales.idverification.modules.watersources.viewmodel

import com.thales.idverification.network.RainFallDetailsResponse



import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.RainFallDetailsPutResponse
import com.thales.idverification.network.RainFallDetailsRequest
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RainFallDetailsViewModel @Inject constructor(private val repository: WOTRRepository) :
    BaseViewModel() {

    fun getRainFallDetailsData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<RainFallDetailsResponse>> {
        return repository.getRainFallDetailsData(villageId, scheduleId)
    }

    fun putRainFallDetailsData(
        villageId: String,
        scheduleId: String,
        rainFallDetailsRequest: RainFallDetailsRequest
    ): LiveData<WOTRCaller<RainFallDetailsPutResponse>> {

        return repository.putRainFallDetailsData(villageId, scheduleId, rainFallDetailsRequest)
    }
}
