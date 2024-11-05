package com.thales.idverification.modules.selectlanguage

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import com.thales.idverification.WaterBudgetApplication
import com.thales.idverification.databinding.ActivitySelectLanguageBinding
import com.thales.idverification.modules.base.BaseActivity
import com.thales.idverification.modules.login.ui.LoginActivity
import com.thales.idverification.utils.WOTRSharedPreference.languageSelected
import com.thales.idverification.utils.WOTRSharedPreference.locale
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectLanguageActivity : BaseActivity() {
    private val ENGLISH_LOCALE = "en"
    private val HINDI_LOCALE = "hi"

    @Inject
    lateinit var wotrPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedLocale = wotrPref.locale
        when (savedLocale) {
            ENGLISH_LOCALE -> selectEnglishOption(binding)
            HINDI_LOCALE -> selectHindiOption(binding)
        }

        binding.card1.setOnClickListener {
            if (savedLocale != ENGLISH_LOCALE) {
                selectEnglishOption(binding)
                setNewLocale(ENGLISH_LOCALE, false)
            }
        }

        binding.card2.setOnClickListener {
            if (savedLocale != HINDI_LOCALE) {
                selectHindiOption(binding)
                setNewLocale(HINDI_LOCALE, false)
            }
        }

        binding.loginButton.setOnClickListener {
            wotrPref.languageSelected = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun selectEnglishOption(binding: ActivitySelectLanguageBinding) {
        binding.card1.setCardBackgroundColor(Color.parseColor("#FF70EAED"))
        binding.card2.setCardBackgroundColor(Color.parseColor("#FFFFFFFF"))
        binding.loginButton.isEnabled = true
    }

    private fun selectHindiOption(binding: ActivitySelectLanguageBinding) {
        binding.card1.setCardBackgroundColor(Color.parseColor("#FFFFFFFF"))
        binding.card2.setCardBackgroundColor(Color.parseColor("#FF70EAED"))
        binding.loginButton.isEnabled = true
    }

    private fun setNewLocale(language: String, restartProcess: Boolean): Boolean {
        WaterBudgetApplication.localeManager.setNewLocale(this, language)
        val i = Intent(this, SelectLanguageActivity::class.java)
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
//        overridePendingTransition(0, 0)
        if (restartProcess) {
            System.exit(0)
        } else {
//            Toast.makeText(this, "Activity restarted", Toast.LENGTH_SHORT).show()
        }
        return true
    }
}