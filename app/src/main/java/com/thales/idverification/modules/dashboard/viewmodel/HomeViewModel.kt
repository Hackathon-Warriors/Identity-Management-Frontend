package com.thales.idverification.modules.dashboard.viewmodel

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.SchedulesData
import com.thales.idverification.network.UserVillageStatusListResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {
    fun getSchedulesData(): LiveData<WOTRCaller<SchedulesData>> {
        return repository.getSchedulesData()
    }

    fun getUserVillageStatusList(
        scheduleId: String
    ): LiveData<WOTRCaller<UserVillageStatusListResponse>> {
        return repository.getUserVillageStatusList(scheduleId)
    }
}
