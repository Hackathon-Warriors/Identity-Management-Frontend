package com.thales.idverification.modules.splash

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.thales.idverification.databinding.ActivitySplashBinding
import com.thales.idverification.modules.base.BaseActivity
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.login.ui.LoginActivity
import com.thales.idverification.modules.selectlanguage.SelectLanguageActivity
import com.thales.idverification.utils.WOTRSharedPreference.languageSelected
import com.thales.idverification.utils.WOTRSharedPreference.sessionToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    @Inject
    lateinit var wotrPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val languageSelected = wotrPref.languageSelected

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if (languageSelected) {
                if (!wotrPref.sessionToken.isNullOrEmpty()) {
                    startActivity(Intent(this, DashboardDrawerActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            } else {
                startActivity(Intent(this, SelectLanguageActivity::class.java))
                finish()
            }
        }, 1000)
    }
}