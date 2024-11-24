package com.thales.idverification.network.retrofit

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.thales.idverification.network.*
import com.thales.idverification.utils.WOTRCaller
import com.thales.idverification.utils.WOTRSharedPreference.sessionToken
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject

class RetrofitNetworkHelperImpl @Inject constructor(private val apiService: ApiService, private val wotrSharedPreferences: SharedPreferences) :
    INetworkHelper {

    override fun verifyUser(verifyUserRequest: VerifyUserRequest): LiveData<WOTRCaller<VerifyUserResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VerifyUserResponse>>()
        apiService.verifyUser(verifyUserRequest).enqueue(object : Callback<VerifyUserResponse> {
            override fun onResponse(
                call: Call<VerifyUserResponse>,
                response: Response<VerifyUserResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        Log.d("verifyUser error: ", response.errorBody().toString())
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<VerifyUserResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun sendOTP(sendOTPRequest: SendOTPRequest): LiveData<WOTRCaller<SendOTPResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<SendOTPResponse>>()
        apiService.sendOTP(sendOTPRequest).enqueue(object : Callback<SendOTPResponse> {
            override fun onResponse(
                call: Call<SendOTPResponse>,
                response: Response<SendOTPResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        Log.d("sendOTP error: ", response.errorBody().toString())
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun loginUser(loginUserRequest: LoginUserRequest): LiveData<WOTRCaller<LoginUserResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<LoginUserResponse>>()
        apiService.loginUser(loginUserRequest).enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        Log.d("loginUser error: ", errorData.toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData = NetworkErrorDataModel(
                            response.message(),
                            response.code().toString()
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun registerUser(registerUserRequest: RegisterUserRequest): LiveData<WOTRCaller<RegisterUserResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<RegisterUserResponse>>()
        apiService.registerUser(registerUserRequest)
            .enqueue(object : Callback<RegisterUserResponse> {
                override fun onResponse(
                    call: Call<RegisterUserResponse>,
                    response: Response<RegisterUserResponse>
                ) {
//                mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            Log.d("registerUser error: ", response.errorBody().toString())
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData = NetworkErrorDataModel(
                                response.message(),
                                response.code().toString()
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }


    override fun getMoistureDataSource(
        villageId: String?,
        scheduleId: String?
    ): LiveData<WOTRCaller<MoistureResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<MoistureResponse>>()
        apiService.getMoistureData(villageId,scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<MoistureResponse> {
                override fun onResponse(
                    call: Call<MoistureResponse>,
                    response: Response<MoistureResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            Log.d("registerUser error: ", response.errorBody().toString())
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData = NetworkErrorDataModel(
                                response.message(),
                                response.code().toString()
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<MoistureResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }


    override fun getSurfaceStorageData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<SurfaceStorageDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<SurfaceStorageDataResponse>>()
        apiService.getSurfaceStorageData(villageId, scheduleId, wotrSharedPreferences.sessionToken).enqueue(object : Callback<SurfaceStorageDataResponse> {
            override fun onResponse(
                call: Call<SurfaceStorageDataResponse>,
                response: Response<SurfaceStorageDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<SurfaceStorageDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getSurfaceStorageMasterData(): LiveData<WOTRCaller<SurfaceStorageMasterDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<SurfaceStorageMasterDataResponse>>()
        apiService.getSurfaceStorageMasterData(wotrSharedPreferences.sessionToken).enqueue(object : Callback<SurfaceStorageMasterDataResponse> {
            override fun onResponse(
                call: Call<SurfaceStorageMasterDataResponse>,
                response: Response<SurfaceStorageMasterDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<SurfaceStorageMasterDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun addSurfaceStorageData(
        villageId: String,
        scheduleId: String,
        surfaceStorageAddDataRequest: SurfaceStorageAddDataRequest
    ): LiveData<WOTRCaller<SurfaceStorageAddDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<SurfaceStorageAddDataResponse>>()
        apiService.addSurfaceStorageData(villageId, scheduleId, surfaceStorageAddDataRequest, wotrSharedPreferences.sessionToken).enqueue(object : Callback<SurfaceStorageAddDataResponse> {
            override fun onResponse(
                call: Call<SurfaceStorageAddDataResponse>,
                response: Response<SurfaceStorageAddDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<SurfaceStorageAddDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getRainFallDetailsData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<RainFallDetailsResponse>> {

        val mutableLiveData = MutableLiveData<WOTRCaller<RainFallDetailsResponse>>()
        apiService.getRainFallDetailsData(villageId, scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<RainFallDetailsResponse> {
                override fun onResponse(
                    call: Call<RainFallDetailsResponse>,
                    response: Response<RainFallDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<RainFallDetailsResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData

    }

    override fun putRainFallDetailsData(
        villageId: String,
        scheduleId: String,
        rainFallDetailsRequest: RainFallDetailsRequest
    ): LiveData<WOTRCaller<RainFallDetailsPutResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<RainFallDetailsPutResponse>>()
        apiService.putRainFallDetailsData(villageId, scheduleId, rainFallDetailsRequest, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<RainFallDetailsPutResponse> {
                override fun onResponse(
                    call: Call<RainFallDetailsPutResponse>,
                    response: Response<RainFallDetailsPutResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            Log.d("putRainfall Error ", errorData.toString())
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData = NetworkErrorDataModel(
                                response.message(),
                                response.code().toString()
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<RainFallDetailsPutResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun getWaterConsumptionMasterData(): LiveData<WOTRCaller<WaterConsumptionMasterDataResponse>> {

        val mutableLiveData = MutableLiveData<WOTRCaller<WaterConsumptionMasterDataResponse>>()
        apiService.getWaterConsumptionMasterData(wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<WaterConsumptionMasterDataResponse> {
                override fun onResponse(
                    call: Call<WaterConsumptionMasterDataResponse>,
                    response: Response<WaterConsumptionMasterDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<WaterConsumptionMasterDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun getVillageWaterUseData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageUseDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VillageUseDataResponse>>()
        apiService.getVillageWaterUseData(villageId, scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<VillageUseDataResponse> {
                override fun onResponse(
                    call: Call<VillageUseDataResponse>,
                    response: Response<VillageUseDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<VillageUseDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun addVillageUseData(
        villageId: String,
        scheduleId: String,
        villageUseAddDataRequest: VillageUseAddDataRequest
    ): LiveData<WOTRCaller<VillageUseAddDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VillageUseAddDataResponse>>()
        apiService.addVillageUseData(villageId, scheduleId, villageUseAddDataRequest, wotrSharedPreferences.sessionToken).enqueue(object : Callback<VillageUseAddDataResponse> {
            override fun onResponse(
                call: Call<VillageUseAddDataResponse>,
                response: Response<VillageUseAddDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<VillageUseAddDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getVillageEvaporationData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<VillageEvaporationResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VillageEvaporationResponse>>()
        apiService.getVillageEvaporationData(villageId, scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<VillageEvaporationResponse> {
                override fun onResponse(
                    call: Call<VillageEvaporationResponse>,
                    response: Response<VillageEvaporationResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<VillageEvaporationResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun addEvaporationData(
        villageId: String,
        scheduleId: String,
        evaporationDataRequest: EvaporationDataRequest
    ): LiveData<WOTRCaller<VillageEvaporationPutResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VillageEvaporationPutResponse>>()
        apiService.addVillageEvaporationData(villageId, scheduleId, evaporationDataRequest, wotrSharedPreferences.sessionToken).enqueue(object : Callback<VillageEvaporationPutResponse> {
            override fun onResponse(
                call: Call<VillageEvaporationPutResponse>,
                response: Response<VillageEvaporationPutResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<VillageEvaporationPutResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getEvaporationMasterData(): LiveData<WOTRCaller<EvaporationMasterDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<EvaporationMasterDataResponse>>()
        apiService.getEvaporationMasterData(wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<EvaporationMasterDataResponse> {
                override fun onResponse(
                    call: Call<EvaporationMasterDataResponse>,
                    response: Response<EvaporationMasterDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<EvaporationMasterDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData

    }

    override fun getOtherSourcesData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<OtherSourcesDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<OtherSourcesDataResponse>>()
        apiService.getOtherSourcesData(villageId, scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<OtherSourcesDataResponse> {
                override fun onResponse(
                    call: Call<OtherSourcesDataResponse>,
                    response: Response<OtherSourcesDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<OtherSourcesDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun getOtherSourcesMasterData(): LiveData<WOTRCaller<OtherSourcesMasterDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<OtherSourcesMasterDataResponse>>()
        apiService.getOtherSourcesMasterData(wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<OtherSourcesMasterDataResponse> {
                override fun onResponse(
                    call: Call<OtherSourcesMasterDataResponse>,
                    response: Response<OtherSourcesMasterDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }


                override fun onFailure(call: Call<OtherSourcesMasterDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun addOtherSourcesData(
        villageId: String,
        scheduleId: String,
        otherSourcesAddDataRequest: OtherSourcesAddDataRequest
    ): LiveData<WOTRCaller<OtherSourcesAddDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<OtherSourcesAddDataResponse>>()
        apiService.addOtherSourcesData(villageId, scheduleId, otherSourcesAddDataRequest, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<OtherSourcesAddDataResponse> {
                override fun onResponse(
                    call: Call<OtherSourcesAddDataResponse>,
                    response: Response<OtherSourcesAddDataResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<OtherSourcesAddDataResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun getCropPlanMasterData(): LiveData<WOTRCaller<CropPlanMasterDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<CropPlanMasterDataResponse>>()
        apiService.getCropPlanMasterData(wotrSharedPreferences.sessionToken).enqueue(object : Callback<CropPlanMasterDataResponse> {
            override fun onResponse(
                call: Call<CropPlanMasterDataResponse>,
                response: Response<CropPlanMasterDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<CropPlanMasterDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getCropPlanDetailsData(villageId: String, scheduleId: String): LiveData<WOTRCaller<CropPlanDetailsResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<CropPlanDetailsResponse>>()
        apiService.getCropPlanDetailsData(villageId, scheduleId, wotrSharedPreferences.sessionToken).enqueue(object : Callback<CropPlanDetailsResponse> {
            override fun onResponse(
                call: Call<CropPlanDetailsResponse>,
                response: Response<CropPlanDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<CropPlanDetailsResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun addCropPlanDetailsData(
        villageId: String,
        scheduleId: String,
        cropPlanDetailsAddDataRequest: CropPlanDetailsAddDataRequest
    ): LiveData<WOTRCaller<CropPlanDetailsAddDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<CropPlanDetailsAddDataResponse>>()
        apiService.addCropPlanDetailsData(villageId, scheduleId, cropPlanDetailsAddDataRequest, wotrSharedPreferences.sessionToken).enqueue(object : Callback<CropPlanDetailsAddDataResponse> {
            override fun onResponse(
                call: Call<CropPlanDetailsAddDataResponse>,
                response: Response<CropPlanDetailsAddDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<CropPlanDetailsAddDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getUserVillageStatusList(
        scheduleId: String
    ): LiveData<WOTRCaller<UserVillageStatusListResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<UserVillageStatusListResponse>>()
        apiService.getUserVillageStatusList(scheduleId, wotrSharedPreferences.sessionToken)
            .enqueue(object : Callback<UserVillageStatusListResponse> {
                override fun onResponse(
                    call: Call<UserVillageStatusListResponse>,
                    response: Response<UserVillageStatusListResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                    } else {
                        if (response.code() in 400..499) {
                            val errorData = GsonBuilder().create().fromJson(
                                response.errorBody()?.string(),
                                NetworkErrorDataModel::class.java
                            )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        } else {
                            val errorData =
                                NetworkErrorDataModel(
                                    response.message(),
                                    response.code().toString()
                                )
                            mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                        }
                    }
                }

                override fun onFailure(call: Call<UserVillageStatusListResponse>, t: Throwable) {
                    mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
                }
            })
        return mutableLiveData
    }

    override fun getSchedulesData(): LiveData<WOTRCaller<SchedulesData>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<SchedulesData>>()
        apiService.getSchedulesData(wotrSharedPreferences.sessionToken).enqueue(object : Callback<SchedulesData> {
            override fun onResponse(
                call: Call<SchedulesData>,
                response: Response<SchedulesData>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<SchedulesData>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun checkLiveliness(
        @Body checkLivelinessRequest: RequestBody
    ): LiveData<WOTRCaller<CheckLiveLinessResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<CheckLiveLinessResponse>>()
        apiService.checkLiveliness(checkLivelinessRequest).enqueue(object : Callback<CheckLiveLinessResponse> {
            override fun onResponse(
                call: Call<CheckLiveLinessResponse>,
                response: Response<CheckLiveLinessResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("live", "Success: "+response.body().toString())
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        Log.d("live", "400 to 499: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData = NetworkErrorDataModel(
                            response.message(),
                            response.code().toString()
                        )
                        Log.d("live", "Error code other than 400: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<CheckLiveLinessResponse>, t: Throwable) {
                Log.d("live", "Failed: "+ t.message.toString())
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun verifyIdentityDocument(
        @Body verifyIdentityDocumentRequest: RequestBody
    ): LiveData<WOTRCaller<VerifyIdentityDocumentResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<VerifyIdentityDocumentResponse>>()
        apiService.verifyIdentityDocument(verifyIdentityDocumentRequest).enqueue(object : Callback<VerifyIdentityDocumentResponse> {
            override fun onResponse(
                call: Call<VerifyIdentityDocumentResponse>,
                response: Response<VerifyIdentityDocumentResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("live", "Success: "+response.body().toString())
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        Log.d("live", "400 to 499: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData = NetworkErrorDataModel(
                            response.message(),
                            response.code().toString()
                        )
                        Log.d("live", "Error code other than 400: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<VerifyIdentityDocumentResponse>, t: Throwable) {
                Log.d("live", "Failed: "+ t.message.toString())
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun checkBankStatement(checkBankStatementRequest: RequestBody): LiveData<WOTRCaller<CheckBankStatementResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<CheckBankStatementResponse>>()
        apiService.checkBankStatement(checkBankStatementRequest).enqueue(object : Callback<CheckBankStatementResponse> {
            override fun onResponse(
                call: Call<CheckBankStatementResponse>,
                response: Response<CheckBankStatementResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("live", "Success: "+response.body().toString())
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        Log.d("live", "400 to 499: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData = NetworkErrorDataModel(
                            response.message(),
                            response.code().toString()
                        )
                        Log.d("live", "Error code other than 400: $errorData")
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<CheckBankStatementResponse>, t: Throwable) {
                Log.d("live", "Failed: "+ t.message.toString())
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }

    override fun getDashboardData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<DashboardDataResponse>> {
        val mutableLiveData = MutableLiveData<WOTRCaller<DashboardDataResponse>>()
        apiService.getDashboardData(
            villageId,
            scheduleId,
            wotrSharedPreferences.sessionToken
        ).enqueue(object : Callback<DashboardDataResponse> {
            override fun onResponse(
                call: Call<DashboardDataResponse>,
                response: Response<DashboardDataResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(WOTRCaller.success(response.body(), null))
                } else {
                    if (response.code() in 400..499) {
                        val errorData = GsonBuilder().create().fromJson(
                            response.errorBody()?.string(),
                            NetworkErrorDataModel::class.java
                        )
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    } else {
                        val errorData =
                            NetworkErrorDataModel(response.message(), response.code().toString())
                        mutableLiveData.postValue(WOTRCaller.success(null, errorData))
                    }
                }
            }

            override fun onFailure(call: Call<DashboardDataResponse>, t: Throwable) {
                mutableLiveData.postValue(WOTRCaller.fail(null, t.message.toString(), null))
            }
        })
        return mutableLiveData
    }
}