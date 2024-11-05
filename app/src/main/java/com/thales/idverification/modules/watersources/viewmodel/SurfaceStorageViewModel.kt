package com.thales.idverification.modules.watersources.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.SurfaceStorageAddDataResponse
import com.thales.idverification.network.SurfaceStorageDataResponse
import com.thales.idverification.network.SurfaceStorageMasterDataResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import com.thales.idverification.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SurfaceStorageViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {

    fun getSurfaceStorageData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<SurfaceStorageDataResponse>> {
        return repository.getSurfaceStorageData(villageId, scheduleId)
    }

    fun getSurfaceStorageMasterData(): LiveData<WOTRCaller<SurfaceStorageMasterDataResponse>> {
        return repository.getSurfaceStorageMasterData()
    }

    fun addSurfaceStorageData(
        villageId: String,
        scheduleId: String,
        structureId: Int?,
        countArea: Double?,
        maxStorageCapacity: Double?,
        actualWaterStored: Double?,
        numberOfFillings: Int?,
        actualStoragePotentialValue: Double?
    ): LiveData<WOTRCaller<SurfaceStorageAddDataResponse>> {
        return repository.addSurfaceStorageData(
            villageId,
            scheduleId,
            structureId,
            countArea,
            maxStorageCapacity,
            actualWaterStored,
            numberOfFillings,
            actualStoragePotentialValue
        )
    }

    fun getWaterStoragePotentialOfStructure(
        waterStoragePotentialOfStructurePerUnit: Double,
        countPerAreaHa: Double
    ): Double = (waterStoragePotentialOfStructurePerUnit * countPerAreaHa).round(2)

    fun getTotalWaterStoredWithMultipleFillings(
        actualWaterStored: Double,
        numberOfFillings: Int
    ): Double = (actualWaterStored * numberOfFillings).round(2)

    fun getTotalWaterInStore(
        totalWaterStoredWithMultipleFillings: Double,
        waterAvailableAfterEvaporation: Double
    ): Double = (totalWaterStoredWithMultipleFillings * waterAvailableAfterEvaporation).round(2)
}
