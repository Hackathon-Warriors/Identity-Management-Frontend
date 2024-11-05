package com.thales.idverification.network.retrofit

import com.thales.idverification.network.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    @POST(ApiConstants.VERIFY_USER)
    fun verifyUser(@Body verifyUserRequest: VerifyUserRequest): Call<VerifyUserResponse>

    @POST(ApiConstants.SEND_OTP)
    fun sendOTP(@Body sendOTPRequest: SendOTPRequest): Call<SendOTPResponse>

    @POST(ApiConstants.LOGIN_USER)
    fun loginUser(@Body loginUserRequest: LoginUserRequest): Call<LoginUserResponse>

    @POST(ApiConstants.REGISTER_USER)
    fun registerUser(@Body registerUserRequest: RegisterUserRequest): Call<RegisterUserResponse>

    @GET("${ApiConstants.GET_MOISTURE_DATA}/{villageId}/{scheduleId}")
    fun getMoistureData(@Path("villageId") villageId: String?, @Path("scheduleId") scheduleId: String?, @Header("Authorization") jwt: String?) : Call<MoistureResponse>

    @GET("${ApiConstants.SURFACE_STORAGE}/{villageId}/{scheduleId}")
    fun getSurfaceStorageData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?): Call<SurfaceStorageDataResponse>

    @GET(ApiConstants.GET_SURFACE_STORAGE_MASTER_DATA)
    fun getSurfaceStorageMasterData(@Header("Authorization") jwt: String?): Call<SurfaceStorageMasterDataResponse>

    @GET("${ApiConstants.VILLAGE_WATER_REQUIREMENT_DATA}/{villageId}/{scheduleId}")
    fun getVillageWaterUseData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?): Call<VillageUseDataResponse>

    @GET("${ApiConstants.VILLAGE_WATER_REQUIREMENT_DATA}/${ApiConstants.MASTER_DATA_URI}")
    fun getWaterConsumptionMasterData(@Header("Authorization") jwt: String?): Call<WaterConsumptionMasterDataResponse>

    @PUT("${ApiConstants.SURFACE_STORAGE}/{villageId}/{scheduleId}")
    fun addSurfaceStorageData(
        @Path("villageId") villageId: String,
        @Path("scheduleId") scheduleId: String,
        @Body addSurfaceStorageDataRequest: SurfaceStorageAddDataRequest,
        @Header("Authorization") jwt: String?
    ): Call<SurfaceStorageAddDataResponse>

    @GET("${ApiConstants.RAINFALL_DATA}/{villageId}/{scheduleId}")
    fun getRainFallDetailsData(@Path("villageId") villageId: String?, @Path("scheduleId") scheduleId: String?, @Header("Authorization") jwt: String?) : Call<RainFallDetailsResponse>

    @PUT("${ApiConstants.RAINFALL_DATA}/{villageId}/{scheduleId}")
    fun putRainFallDetailsData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Body rainFallDetailsRequest: RainFallDetailsRequest, @Header("Authorization") jwt: String?) : Call<RainFallDetailsPutResponse>

    @PUT("${ApiConstants.VILLAGE_WATER_REQUIREMENT_DATA}/{villageId}/{scheduleId}")
    fun addVillageUseData(
        @Path("villageId") villageId: String,
        @Path("scheduleId") scheduleId: String,
        @Body addVillageUseAddDataRequest: VillageUseAddDataRequest,
        @Header("Authorization") jwt: String?
    ): Call<VillageUseAddDataResponse>

    @GET("${ApiConstants.EVAPORATION_BASE_URI}/{villageId}/{scheduleId}")
    fun getVillageEvaporationData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?): Call<VillageEvaporationResponse>

    @PUT("${ApiConstants.EVAPORATION_BASE_URI}/{villageId}/{scheduleId}")
    fun addVillageEvaporationData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Body evaporationDataRequest: EvaporationDataRequest, @Header("Authorization") jwt: String?): Call<VillageEvaporationPutResponse>


    @GET("${ApiConstants.EVAPORATION_BASE_URI}/${ApiConstants.MASTER_DATA_URI}")
    fun getEvaporationMasterData(@Header("Authorization") jwt: String?): Call<EvaporationMasterDataResponse>

    @GET("${ApiConstants.OTHER_SOURCES_DATA}/{villageId}/{scheduleId}")
    fun getOtherSourcesData(
        @Path("villageId") villageId: String,
        @Path("scheduleId") scheduleId: String,
        @Header("Authorization") jwt: String?
    ): Call<OtherSourcesDataResponse>

    @GET(ApiConstants.GET_OTHER_SOURCES_MASTER_DATA)
    fun getOtherSourcesMasterData(@Header("Authorization") jwt: String?): Call<OtherSourcesMasterDataResponse>

    @PUT("${ApiConstants.OTHER_SOURCES_DATA}/{villageId}/{scheduleId}")
    fun addOtherSourcesData(
        @Path("villageId") villageId: String,
        @Path("scheduleId") scheduleId: String,
        @Body addOtherSourcesAddDataRequest: OtherSourcesAddDataRequest,
        @Header("Authorization") jwt: String?
    ): Call<OtherSourcesAddDataResponse>

    @GET(ApiConstants.GET_CROP_PLAN_MASTER_DATA)
    fun getCropPlanMasterData(@Header("Authorization") jwt: String?): Call<CropPlanMasterDataResponse>

    @GET("${ApiConstants.CROP_PLAN_DETAILS}/{villageId}/{scheduleId}")
    fun getCropPlanDetailsData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?): Call<CropPlanDetailsResponse>

    @PUT("${ApiConstants.CROP_PLAN_DETAILS}/{villageId}/{scheduleId}")
    fun addCropPlanDetailsData(
        @Path("villageId") villageId: String,
        @Path("scheduleId") scheduleId: String,
        @Body cropPlanDetailsAddDataRequest: CropPlanDetailsAddDataRequest,
        @Header("Authorization") jwt: String?
    ): Call<CropPlanDetailsAddDataResponse>

    @GET("user/villages/schedule/{scheduleId}")
    fun getUserVillageStatusList(@Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?) : Call<UserVillageStatusListResponse>

    @GET(ApiConstants.GET_SCHEDULES_DATA)
    fun getSchedulesData(@Header("Authorization") jwt: String?): Call<SchedulesData>

    @GET("${ApiConstants.GET_DASHBOARD_DATA}/{villageId}/{scheduleId}")
    fun getDashboardData(@Path("villageId") villageId: String, @Path("scheduleId") scheduleId: String, @Header("Authorization") jwt: String?): Call<DashboardDataResponse>

}