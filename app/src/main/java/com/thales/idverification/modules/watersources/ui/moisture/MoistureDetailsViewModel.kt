package com.thales.idverification.modules.watersources.ui.moisture

import androidx.lifecycle.LiveData
import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.network.MoistureResponse
import com.thales.idverification.repository.WOTRRepository
import com.thales.idverification.utils.WOTRCaller
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoistureDetailsViewModel @Inject constructor(private val repository: WOTRRepository) : BaseViewModel()  {

        fun getMoistureSourceData(
            villageId: String,
            scheduleId: String
        ): LiveData<WOTRCaller<MoistureResponse>> {
            return repository.getMoistureSourceData(villageId,scheduleId)
        }

}