package com.thales.idverification.modules.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.thales.idverification.WaterBudgetApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(WaterBudgetApplication.localeManager.setLocale(base))
    }
}