package com.thales.idverification

import android.app.Application
import android.content.Context
import com.thales.idverification.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WaterBudgetApplication: Application() {
    companion object{
        lateinit var localeManager: LocaleManager
        private lateinit var mContext: Context
        fun getContext(): Context = mContext
    }

    override fun attachBaseContext(base: Context?) {
        localeManager = LocaleManager(base!!);
        super.attachBaseContext(localeManager.setLocale(base));
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }
}