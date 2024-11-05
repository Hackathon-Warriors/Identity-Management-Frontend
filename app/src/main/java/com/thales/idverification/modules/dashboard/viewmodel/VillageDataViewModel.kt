package com.thales.idverification.modules.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.DashboardDataResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VillageDataViewModel @Inject constructor(private val repository: WOTRRepository): BaseViewModel() {

    var displayedChild: MutableLiveData<Int> = MutableLiveData(0)

    fun getSourcesData(
        villageId: String,
        scheduleId: String
    ): LiveData<WOTRCaller<DashboardDataResponse>> {
        return repository.getDashboardData(villageId, scheduleId)
    }
}