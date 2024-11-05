package com.thales.idverification.network


import com.thales.idverification.utils.WOTRCaller
import androidx.lifecycle.LiveData

interface IVillages {
    fun getUserVillageStatusList(scheduleId: String) : LiveData<WOTRCaller<UserVillageStatusListResponse>>
}
