package com.thales.idverification.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
//import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parcelize

@Parcelize
data class VerifyUserRequest(
    val mobileNumber: String? = null
) : Parcelable

@Parcelize
data class VerifyUserResponse(
    val isUserRegistered: Boolean? = null
) : Parcelable

@Parcelize
data class SendOTPRequest(
    val mobileNumber: String? = null,
    val isUserRegistered: Boolean? = null
) : Parcelable

@Parcelize
data class SendOTPResponse(
    val otpSent: Boolean? = null,
    val otpTimestamp: Double? = null
) : Parcelable

@Parcelize
data class LoginUserRequest(
    val mobileNumber: String? = null,
    val otp: String? = null
) : Parcelable

@Parcelize
data class LoginUserResponse(
    val isSuccess: String? = null,
    val token: String? = null
) : Parcelable

@Parcelize
data class RegisterUserRequest(
    val email: String? = null,
    val mobileNumber: String? = null,
    val isUserRegistered: Boolean? = null,
    val fullName: String? = null,
    val otp: String? = null
) : Parcelable

@Parcelize
data class RegisterUserResponse(
    val isSuccess: Boolean? = null
) : Parcelable

@Parcelize
data class ErrorResponse(
    val errorReason: String? = null,
    val errorCode: String? = null
) : Parcelable

@Parcelize
data class MoistureResponse(
    val villageId: String? = null,
    val scheduleId: String? = null,
    val soilMoisture: Double? = null,
    val groundWater: Double? = null
) : Parcelable

@Parcelize
data class SurfaceStorageDataResponse(
    val villageId: String? = null,
    val scheduleId: String? = null,
    val structures: List<Structures> = listOf()
) : Parcelable

@Parcelize
data class Structures(
    val structureName: String? = null,
    val structureId: Int? = null,
    val countArea: Double? = null,
    val maxStorageCapacity: Double? = null,
    val actualWaterStored: Double? = null,
    val numberOfFillings: Int? = null,
    val totalWaterStoredWithFillings: Double? = null,
    val storagePotentialConstant: Double? = null,
    val evaporation: Double? = null,
    val totalWaterStored: Double? = null,
    val actualStoragePotentialValue: Double? = null
) : Parcelable

@Parcelize
data class VillageUseDataResponse(
    val villageId: String? = null,
    val scheduleId: String? = null,
    val structures: List<VillageUseStructures> = listOf()
) : Parcelable

@Parcelize
data class VillageUseStructures(
    val structureName: String? = null,
    val structureId: Int? = null,
    val perDayWaterRequirement: Double? = null,
    val totalWaterRequirementForDomesticUse: Double? = null
) : Parcelable

@Parcelize
data class SurfaceStorageMasterDataResponse(
    val surfaceWaterMasterDataList: List<SurfaceWaterMasterData> = listOf()
) : Parcelable

@Parcelize
data class SurfaceWaterMasterData(
    val structureId: Int? = null,
    val structureName: String? = null,
    val storagePotentialConstant: Double? = null,
    val evaporationConstant: Double? = null
) : Parcelable

@Parcelize
data class SurfaceStorageAddDataRequest(
    val villageId: Int? = null,
    val structureId: Int? = null,
    val countArea: Double? = null,
    val maxStorageCapacity: Double? = null,
    val actualWaterStored: Double? = null,
    val numberOfFillings: Int? = null,
    val actualStoragePotentialValue: Double? = null
) : Parcelable

@Parcelize
data class SurfaceStorageAddDataResponse(
    val status: String? = null
) : Parcelable

@Parcelize
data class RainFallDetailsResponse(
    val villageId: String? = null,
    val villageArea: Double? = null,
    val rainfall: Double? = null,
    val totalWaterAvailable: Double? = null
) : Parcelable

@Parcelize
data class RainFallDetailsRequest(
    val rainfall: Double? = null,
    val villageArea: Double? = null,
    val villageId: Int? = null

) : Parcelable

@Parcelize
data class EvaporationDataRequest(
    val villageId: Int? = null,
    val scheduleId: Int? = null,
    val demandEvaporationDataId: Int? = null,
    val areaInHectare: Double? = null

) : Parcelable


@Parcelize
data class RainFallDetailsPutResponse(
    val status: String?
) : Parcelable

@Parcelize
data class VillageUseAddDataRequest(
    val villageId: Int? = null,
    val structureId: Int? = null,
    val perDayWaterRequirement: Double? = null,
    val totalNumberOfStructure: Double? = null,
    val annualWaterRequirement: Double? = null,
    val totalWaterRequirementForDomesticUse: Double? = null
) : Parcelable

@Parcelize
data class VillageUseAddDataResponse(
    val status: String? = null
) : Parcelable

@Parcelize
data class VillageEvaporationResponse(
    val villageId: Int? = null,
    val structureId: Int? = null,
    val waterEvaporationDataList: List<VillageEvaporationDataList> = listOf()
) : Parcelable

@Parcelize
data class VillageEvaporationPutResponse(
    val status: String? = null,
    val isUpdated: Boolean? = false
) : Parcelable

@Parcelize
data class VillageEvaporationDataList(
    val demandEvaporationDataId: Int? = null,
    val areaInHectare: Double? = null,
    val evaporationAreaType: String? = null,
    val evaporationConstant: Double? = null,
    val totalWaterEvaporation: Double? = null
) : Parcelable

@Parcelize
data class EvaporationMasterDataResponse(
    val waterEvaporationData: List<MasterEvaporationDataList> = listOf()
) : Parcelable

@Parcelize
data class MasterEvaporationDataList(
    val evaporationMasterId: Int? = null,
    val evaporationAreaType: String? = null,
    val evaporationConstant: Double? = null
) : Parcelable

@Parcelize
data class WaterConsumptionMasterDataResponse(
    val waterConsumptionMaster: List<WaterConsumptionMasterDataList> = listOf()
) : Parcelable

@Parcelize
data class WaterConsumptionMasterDataList(
    val waterUserMasterId: Int? = null,
    val waterUserMasterName: String? = null,
    val dailyWaterRequirement: Double? = null
) : Parcelable


@Parcelize
data class OtherSourcesDataResponse(
    val villageId: String? = null,
    val scheduleId: String? = null,
    @SerializedName("structures")
    val sources: List<Sources> = listOf()
) : Parcelable

@Parcelize
data class Sources(
    val sourceId: String? = null,
    val sourceName: String? = null,
    val numberOfPumps: Int? = null,
    val dischargePerPump: Double? = null,
    val numberOfPumpingDaysinYear: Int? = null,
    val avgHoursPumpingPerDay: Int? = null,
    val totalWaterImport: Double? = null
) : Parcelable


@Parcelize
data class OtherSourcesMasterDataResponse(
    val otherSourcesMasterDataList: List<OtherSourcesMasterData> = listOf()
) : Parcelable

@Parcelize
data class OtherSourcesMasterData(
    val sourceId: Int? = null,
    val sourceName: String? = null
) : Parcelable


@Parcelize
data class OtherSourcesAddDataRequest(
    val villageId: Int? = null,
    val sourceId: String? = null,
    val numberOfPumps: Int? = null,
    val dischargePerPump: Double? = null,
    val numberOfPumpingDaysinYear: Int? = null,
    val avgHoursPumpingPerDay: Int? = null
) : Parcelable

@Parcelize
data class OtherSourcesAddDataResponse(
    val status: String? = null
) : Parcelable


@Parcelize
data class CropPlanDetailsResponse(
    val cropDataList: List<CropData> = listOf()
) : Parcelable

@Parcelize
data class CropData(
    val cropDataId: Int? = null,
    val cropId: Int? = null,
    val cropArea: Double? = null,
    val rainfedFloodArea: Double? = null,
    val cropName: String? = null,
    val waterReqConstant: Double? = null,
    val microIrrigationArea: Double? = null,
    val microWaterReqConstant: Double? = null,
    val waterReqRainfedCrop: Double? = null,
    val waterReqMicroCrop: Double? = null,
    val totalWaterRequirement: Double? = null
) : Parcelable

@Parcelize
data class CropPlanMasterDataResponse(
    val cropMasterDataList: List<CropMasterData> = listOf()
) : Parcelable

@Parcelize
data class CropMasterData(
    val cropId: Int? = null,
    val cropName: String? = null,
    val cropType: String? = null,
    val waterRequirementConstant: Double? = null
) : Parcelable

@Parcelize
data class CropPlanDetailsAddDataResponse(
    val status: String? = null
) : Parcelable

@Parcelize
data class CropPlanDetailsAddDataRequest(
    val cropDataId: Int? = null,
    val villageId: Int? = null,
    val scheduleId: Int? = null,
    val cropId: Int? = null,
    val cropArea: Double? = null,
    val rainfedFloodArea: Double? = null
) : Parcelable

@Parcelize
data class UserVillageStatusListResponse(
    val userVillageStatusList: List<UserVillageStatusListData> = listOf()
) : Parcelable

@Parcelize
data class UserVillageStatusListData(
    val userId: Int?,
    val roleName: String,
    val roleId: Int,
    val villageId: Int,
    val villageName: String,
    val status: String
) : Parcelable
@Parcelize
data class SchedulesData(
    val schedules: List<Schedule> = listOf()
): Parcelable

@Parcelize
data class Schedule(
    val scheduleId: Int? = null,
    val year: String? = null,
    val month: String? = null
): Parcelable

@Parcelize
data class DashboardDataResponse(
    val totalWaterAvailable: Double? = 0.0,
    val sources: DashboardSourcesData? = null,
    val consumption: DashboardConsumptionData? = null,
    val cropPlan: DashboardCropPlanData? = null
):Parcelable

@Parcelize
data class DashboardSourcesData(
    val totalWaterAvailable: Double? = 0.0,
    val rainfall: Double? = 0.0,
    val moistureAndGroundWater: Double? = 0.0,
    val surfaceStorage: Double? = 0.0,
    val otherStorage: Double? = 0.0
):Parcelable

@Parcelize
data class DashboardConsumptionData(
    val totalWaterRequirementForDomesticUse: Double? = 0.0,
    val totalEvaporationWaterConsumption: Double? = 0.0,
    val totalConsumption: Double? = 0.0
):Parcelable

@Parcelize
data class DashboardCropConsumptionKharifData(
    val totalWaterRequirement: Double? = 0.0
):Parcelable

@Parcelize
data class DashboardCropConsumptionRabiData(
    val totalWaterRequirement: Double? = 0.0
):Parcelable

@Parcelize
data class DashboardCropConsumptionData(
    val Kharif: DashboardCropConsumptionKharifData? = null,
    val Rabi: DashboardCropConsumptionRabiData? = null,
):Parcelable

@Parcelize
data class DashboardCropPlanData(
    val totalCropWaterRequired: Double? = 0.0,
    val cropConsumption: DashboardCropConsumptionData? = null,
):Parcelable

@Parcelize
data class CheckLiveLinessResponse(
    val errorMsg: String,
    val msg: String,
    val success: Boolean
):Parcelable

@Parcelize
data class VerifyIdentityDocumentResponse(
    val errorMsg: String,
    val msg: String,
    val success: Boolean
):Parcelable

@Parcelize
data class CheckBankStatementResponse(
    val errorMsg: String,
    val msg: String,
    val success: Boolean
):Parcelable
