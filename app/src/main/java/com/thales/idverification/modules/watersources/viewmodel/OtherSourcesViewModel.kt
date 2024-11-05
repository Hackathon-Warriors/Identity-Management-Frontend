package com.thales.idverification.modules.watersources.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.OtherSourcesAddDataResponse
import com.thales.idverification.network.OtherSourcesDataResponse
import com.thales.idverification.network.OtherSourcesMasterDataResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtherSourcesViewModel @Inject constructor(private val repository: WOTRRepository) :
    BaseViewModel() {

    fun getOtherSourcesData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<OtherSourcesDataResponse>> {
        return repository.getOtherSourcesData(villageId, scheduleId)
    }

    fun getOtherSourcesMasterData(): LiveData<WOTRCaller<OtherSourcesMasterDataResponse>> {
        return repository.getOtherSourcesMasterData()
    }

    fun addOtherSourcesData(
        villageId: String,
        scheduleId: String,
        sourceId: String,
        numberOfPumps: Int?,
        dischargePerPump: Double?,
        numberOfPumpingDaysinYear: Int?,
        avgHoursPumpingPerDay: Int?
    ): LiveData<WOTRCaller<OtherSourcesAddDataResponse>> {
        return repository.addOtherSourcesData(
            villageId,
            scheduleId,
            sourceId,
            numberOfPumps,
            dischargePerPump,
            numberOfPumpingDaysinYear,
            avgHoursPumpingPerDay
        )
    }

}