package com.thales.idverification.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Build.VERSION_CODES.N
import android.os.LocaleList
import com.thales.idverification.utils.WOTRSharedPreference.locale
import java.util.*

class LocaleManager(context: Context) {


   var wotrPref: SharedPreferences = WOTRSharedPreference.defaultPreference(context)

    fun setLocale(c: Context?): Context? {
        return updateResources(c!!, wotrPref.locale?:"en")
    }

    private fun updateResources(context: Context, language: String): Context? {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        when {
            isAtLeastVersion(N) -> {
                config.setLocales(LocaleList(locale))
                context = context.createConfigurationContext(config)
            }
            isAtLeastVersion(JELLY_BEAN_MR1) -> {
                config.setLocale(locale)
                context = context.createConfigurationContext(config)
            }
            else -> {
                /*config.locale = locale
                res.updateConfiguration(config, res.displayMetrics)*/
            }
        }
        return context
    }

    private fun isAtLeastVersion(version: Int): Boolean {
        return Build.VERSION.SDK_INT >= version
    }

    fun setNewLocale(context: Context, language: String): Context? {
        wotrPref.locale = language
        return updateResources(context, language);
    }

    fun getLocale(res: Resources): Locale? {
        val config = res.configuration
        return if (isAtLeastVersion(N)) config.locales[0] else config.locale
    }


}