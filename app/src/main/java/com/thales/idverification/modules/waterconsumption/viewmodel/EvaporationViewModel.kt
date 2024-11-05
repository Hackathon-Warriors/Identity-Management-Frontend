package com.thales.idverification.modules.waterconsumption.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.EvaporationMasterDataResponse
import com.thales.idverification.network.VillageEvaporationPutResponse
import com.thales.idverification.network.VillageEvaporationResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EvaporationViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {

    fun getVillageEvaporationData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageEvaporationResponse>> {
        return repository.getVillageEvaporationData(villageId, scheduleId)
    }

    fun getEvaporationMasterData(): LiveData<WOTRCaller<EvaporationMasterDataResponse>> {
        return repository.getEvaporationMasterData()
    }

    fun addVillageEvaporationData(
        villageId: String,
        scheduleId: String,
        demandEvaporationDataId: Int?,
        areaInHectare: Double?
    ): LiveData<WOTRCaller<VillageEvaporationPutResponse>>{
        return repository.addVillageEvaporationData(villageId, scheduleId, demandEvaporationDataId, areaInHectare)
    }

}

