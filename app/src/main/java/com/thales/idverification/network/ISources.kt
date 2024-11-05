package com.thales.idverification.network

import androidx.lifecycle.LiveData
import com.thales.idverification.utils.WOTRCaller

interface ISources {
    fun getMoistureDataSource(villageId: String?, scheduleId: String?) : LiveData<WOTRCaller<MoistureResponse>>
    fun getSurfaceStorageData(villageId: String, scheduleId: String): LiveData<WOTRCaller<SurfaceStorageDataResponse>>
    fun getSurfaceStorageMasterData(): LiveData<WOTRCaller<SurfaceStorageMasterDataResponse>>
    fun addSurfaceStorageData(
        villageId: String,
        scheduleId: String,
        surfaceStorageAddDataRequest: SurfaceStorageAddDataRequest
    ): LiveData<WOTRCaller<SurfaceStorageAddDataResponse>>
    fun getRainFallDetailsData(villageId: String, scheduleId: String): LiveData<WOTRCaller<RainFallDetailsResponse>>
    fun putRainFallDetailsData(villageId: String, scheduleId: String, rainFallDetailsRequest: RainFallDetailsRequest) : LiveData<WOTRCaller<RainFallDetailsPutResponse>>
    fun getWaterConsumptionMasterData(): LiveData<WOTRCaller<WaterConsumptionMasterDataResponse>>
    fun getVillageWaterUseData(villageId: String, scheduleId: String): LiveData<WOTRCaller<VillageUseDataResponse>>
    fun addVillageUseData(
        villageId: String,
        scheduleId: String,
        villageUseAddDataRequest: VillageUseAddDataRequest
    ): LiveData<WOTRCaller<VillageUseAddDataResponse>>
    fun getVillageEvaporationData(villageId: String, scheduleId: String): LiveData<WOTRCaller<VillageEvaporationResponse>>
    fun getEvaporationMasterData(): LiveData<WOTRCaller<EvaporationMasterDataResponse>>
    fun getOtherSourcesData(villageId: String, scheduleId: String): LiveData<WOTRCaller<OtherSourcesDataResponse>>
    fun getOtherSourcesMasterData(): LiveData<WOTRCaller<OtherSourcesMasterDataResponse>>
    fun addOtherSourcesData(
        villageId: String,
        scheduleId: String,
        otherSourcesAddDataRequest: OtherSourcesAddDataRequest
    ): LiveData<WOTRCaller<OtherSourcesAddDataResponse>>
    fun getDashboardData(villageId: String, scheduleId: String): LiveData<WOTRCaller<DashboardDataResponse>>
    fun addEvaporationData(
        villageId: String,
        scheduleId: String,
        evaporationDataRequest: EvaporationDataRequest
    ): LiveData<WOTRCaller<VillageEvaporationPutResponse>>

}