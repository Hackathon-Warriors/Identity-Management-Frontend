package com.thales.idverification.modules.cropplan.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.CropPlanDetailsAddDataResponse
import com.thales.idverification.network.CropPlanDetailsResponse
import com.thales.idverification.network.CropPlanMasterDataResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import com.thales.idverification.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CropDetailsViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {

    fun getCropPlanDetailsData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<CropPlanDetailsResponse>> {
        return repository.getCropPlanDetailsData(villageId, scheduleId)
    }

    fun getCropPlanMasterData(): LiveData<WOTRCaller<CropPlanMasterDataResponse>> {
        return repository.getCropPlanMasterData()
    }

    fun addCropPlanDetailsData(
        cropDataId: Int?,
        villageId: String,
        scheduleId: String,
        cropId: Int?,
        cropArea: Double?,
        rainfedFloodArea: Double?
    ): LiveData<WOTRCaller<CropPlanDetailsAddDataResponse>> {
        return repository.addCropPlanDetailsData(
            cropDataId,
            villageId,
            scheduleId,
            cropId,
            cropArea,
            rainfedFloodArea
        )
    }

    fun getWaterRequiredForRainfedAndFloodIrrigation(
        rainfedOrFloodIrrigationArea: Double,
        perHectareCropwaterRequirement: Double
    ): Double = (rainfedOrFloodIrrigationArea * perHectareCropwaterRequirement).round(2)

    fun getMicroIrrigationArea(
        cropArea: Double,
        rainfedOrFloodIrrigationArea: Double
    ): Double {
        return (cropArea - rainfedOrFloodIrrigationArea).round(2)
    }

    fun getPerHectareCropwaterRequiredUnderMicroIrrigation(
        perHectareCropwaterRequirement: Double
    ): Double = perHectareCropwaterRequirement * 0.6

    fun getWaterRequiredForMicroIrrigationArea(
        microIrrigationArea: Double,
        perHectareCropwaterRequirementForMicroIrrigationArea: Double
    ): Double =
        (microIrrigationArea * perHectareCropwaterRequirementForMicroIrrigationArea).round(2)

    fun getTotalWaterRequiredForCrops(
        waterRequiredForRainfedAndFloodIrrigation: Double,
        waterRequiredForMicroIrrigationArea: Double
    ): Double = (waterRequiredForRainfedAndFloodIrrigation + waterRequiredForMicroIrrigationArea).round(2)
}
