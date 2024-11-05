package com.thales.idverification.network

import androidx.lifecycle.LiveData
import com.thales.idverification.utils.WOTRCaller

interface ICropPlan {
    fun getCropPlanMasterData(): LiveData<WOTRCaller<CropPlanMasterDataResponse>>
    fun getCropPlanDetailsData(villageId: String, scheduleId: String): LiveData<WOTRCaller<CropPlanDetailsResponse>>
    fun addCropPlanDetailsData(villageId: String, scheduleId: String, cropPlanDetailsAddDataRequest: CropPlanDetailsAddDataRequest): LiveData<WOTRCaller<CropPlanDetailsAddDataResponse>>
}
