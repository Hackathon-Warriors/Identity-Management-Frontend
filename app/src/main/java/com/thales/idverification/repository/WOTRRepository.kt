package com.thales.idverification.repository

import androidx.lifecycle.LiveData
import com.thales.idverification.network.*
import com.thales.idverification.utils.WOTRCaller
import javax.inject.Inject

class WOTRRepository @Inject constructor(private val networkHelper: INetworkHelper) {

    fun verifyUser(mobileNumber: String): LiveData<WOTRCaller<VerifyUserResponse>> {
        return networkHelper.verifyUser(VerifyUserRequest(mobileNumber))
    }

    fun sendOTP(mobileNumber: String,
                   isUserRegistered: Boolean): LiveData<WOTRCaller<SendOTPResponse>> {
        return networkHelper.sendOTP(SendOTPRequest(mobileNumber, isUserRegistered))
    }

    fun loginUser(mobileNumber: String,
                  otp: String): LiveData<WOTRCaller<LoginUserResponse>> {
        return networkHelper.loginUser(LoginUserRequest(mobileNumber, otp))
    }

    fun registerUser(emailAddress: String,
                     mobileNumber: String,
                     isUserRegistered: Boolean,
                     fullName: String,
                     otp: String): LiveData<WOTRCaller<RegisterUserResponse>> {
        return networkHelper.registerUser(
            RegisterUserRequest(
                emailAddress,
                mobileNumber,
                isUserRegistered,
                fullName,
                otp
            )
        )
    }

    fun getMoistureSourceData(villageId: String?, scheduleId: String?) : LiveData<WOTRCaller<MoistureResponse>> {
        return networkHelper.getMoistureDataSource(villageId,scheduleId)
    }

    fun getSurfaceStorageData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<SurfaceStorageDataResponse>> {
        return networkHelper.getSurfaceStorageData(
            villageId,
            scheduleId
        )
    }

    fun getSurfaceStorageMasterData(): LiveData<WOTRCaller<SurfaceStorageMasterDataResponse>> {
        return networkHelper.getSurfaceStorageMasterData()
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
        return networkHelper.addSurfaceStorageData(
            villageId,
            scheduleId,
            SurfaceStorageAddDataRequest(
                villageId.toInt(),
                structureId,
                countArea,
                maxStorageCapacity,
                actualWaterStored,
                numberOfFillings,
                actualStoragePotentialValue
            )
        )
    }

    fun getRainFallDetailsData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<RainFallDetailsResponse>> {
        return networkHelper.getRainFallDetailsData(
            villageId,
            scheduleId
        )
    }

    fun putRainFallDetailsData(
        villageId: String,
        scheduleId: String,
        rainFallDetailsRequest: RainFallDetailsRequest
    ): LiveData<WOTRCaller<RainFallDetailsPutResponse>> {
        return networkHelper.putRainFallDetailsData(
            villageId,
            scheduleId,
            rainFallDetailsRequest
        )
    }

    fun getVillageUseData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageUseDataResponse>> {
        return networkHelper.getVillageWaterUseData(
            villageId,
            scheduleId
        )
    }

    fun getWaterConsumptionMasterData(): LiveData<WOTRCaller<WaterConsumptionMasterDataResponse>> {
        return networkHelper.getWaterConsumptionMasterData()
    }

    fun addVillageUseData(
        villageId: String,
        scheduleId: String,
        structureId: Int?,
        perDayWaterRequirement: Double?,
        totalNumberOfStructure: Double?,
        annualWaterRequirement: Double?
    ): LiveData<WOTRCaller<VillageUseAddDataResponse>> {
        return networkHelper.addVillageUseData(
            villageId,
            scheduleId,
            VillageUseAddDataRequest(
                villageId.toInt(),
                structureId,
                perDayWaterRequirement,
                totalNumberOfStructure,
                annualWaterRequirement
            )
        )
    }

    fun getVillageEvaporationData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageEvaporationResponse>> {
        return networkHelper.getVillageEvaporationData(
            villageId,
            scheduleId
        )
    }

    fun getEvaporationMasterData(): LiveData<WOTRCaller<EvaporationMasterDataResponse>> {
        return networkHelper.getEvaporationMasterData()
    }

    fun addVillageEvaporationData(
        villageId: String,
        scheduleId: String,
        demandEvaporationDataId: Int?,
        areaInHectare: Double?
    ): LiveData<WOTRCaller<VillageEvaporationPutResponse>> {
        return networkHelper.addEvaporationData(
            villageId,
            scheduleId,
            EvaporationDataRequest(
                villageId.toInt(),
                scheduleId.toInt(),
                demandEvaporationDataId,
                areaInHectare
            )
        )
    }

    fun getOtherSourcesData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<OtherSourcesDataResponse>> {
        return networkHelper.getOtherSourcesData(villageId, scheduleId)
    }

    fun getOtherSourcesMasterData(): LiveData<WOTRCaller<OtherSourcesMasterDataResponse>> {
        return networkHelper.getOtherSourcesMasterData()
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
        return networkHelper.addOtherSourcesData(
            villageId,
            scheduleId,
            OtherSourcesAddDataRequest(
                villageId.toInt(),
                sourceId,
                numberOfPumps,
                dischargePerPump,
                numberOfPumpingDaysinYear,
                avgHoursPumpingPerDay
            )
        )
    }

    fun getCropPlanMasterData(): LiveData<WOTRCaller<CropPlanMasterDataResponse>> {
        return networkHelper.getCropPlanMasterData()
    }

    fun getCropPlanDetailsData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<CropPlanDetailsResponse>> {
        return networkHelper.getCropPlanDetailsData(
            villageId,
            scheduleId
        )
    }

    fun addCropPlanDetailsData(
        cropDataId: Int?,
        villageId: String,
        scheduleId: String,
        cropId: Int?,
        cropArea: Double?,
        rainfedFloodArea: Double?
    ): LiveData<WOTRCaller<CropPlanDetailsAddDataResponse>> {
        return networkHelper.addCropPlanDetailsData(
            villageId,
            scheduleId,
            CropPlanDetailsAddDataRequest(
                cropDataId,
                villageId.toInt(),
                scheduleId.toInt(),
                cropId,
                cropArea,
                rainfedFloodArea
            )
        )
    }

   fun getUserVillageStatusList(
    scheduleId: String
    ): LiveData<WOTRCaller<UserVillageStatusListResponse>> {
       return networkHelper.getUserVillageStatusList(scheduleId)
   }

    fun getSchedulesData(): LiveData<WOTRCaller<SchedulesData>> {
        return networkHelper.getSchedulesData()
    }

    fun getDashboardData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<DashboardDataResponse>> {
        return networkHelper.getDashboardData(villageId, scheduleId)
    }
}