package com.thales.idverification.network

import androidx.lifecycle.LiveData
import com.thales.idverification.utils.WOTRCaller

interface IVillagesListScreen {
    fun getSchedulesData(): LiveData<WOTRCaller<SchedulesData>>
}
