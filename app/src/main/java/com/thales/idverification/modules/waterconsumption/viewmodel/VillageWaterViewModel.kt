package com.thales.idverification.modules.waterconsumption.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.VillageUseDataResponse
import com.thales.idverification.network.VillageUseAddDataResponse
import com.thales.idverification.network.WaterConsumptionMasterDataResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VillageWaterViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {

    fun getVillageUseData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageUseDataResponse>> {
        return repository.getVillageUseData(villageId, scheduleId)
    }

    fun getWaterConsumptionMasterData(): LiveData<WOTRCaller<WaterConsumptionMasterDataResponse>>{
        return repository.getWaterConsumptionMasterData()
    }

    fun addVillageUseData(
        villageId: String,
        scheduleId: String,
        structureId: Int?,
        perDayWaterRequirement: Double?,
        totalNumberOfStructure: Double?,
        annualWaterRequirement: Double?
    ): LiveData<WOTRCaller<VillageUseAddDataResponse>> {
        return repository.addVillageUseData(
            villageId,
            scheduleId,
            structureId,
            perDayWaterRequirement,
            totalNumberOfStructure,
            annualWaterRequirement
        )
    }

}

