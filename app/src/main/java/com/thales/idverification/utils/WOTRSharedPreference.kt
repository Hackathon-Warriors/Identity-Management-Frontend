package com.thales.idverification.utils

import android.content.Context
import android.content.SharedPreferences

object WOTRSharedPreference {

    private const val USER_PREF = "user_data"

    // Select Language
    private const val LOCALE = "selected_locale"
    private const val LANGUAGE_SELECTED = "language_selected"

    // User Data
    private const val USER_MOBILE = "mobile_number"
    private const val USER_EMAIL = "email_address"
    private const val USER_FULLNAME = "full_name"
    private const val USER_REGISTERED = "user_registered"

    // Session
    private const val SESSION_TOKEN = "session_token"


    private fun customPreference(context: Context,
                                 name: String
    ): SharedPreferences = context
        .getSharedPreferences(name, Context.MODE_PRIVATE)

    fun defaultPreference(context: Context): SharedPreferences = customPreference(
        context,
        USER_PREF
    )

    private inline fun SharedPreferences.editPref(operation: (SharedPreferences.Editor) -> Unit) {
        val editPref = edit()
        operation(editPref)
        editPref.apply()
    }

    var SharedPreferences.locale
        get() = getString(LOCALE, "en")
        set(value) {
            editPref {
                it.putString(LOCALE, value)
            }
        }

    var SharedPreferences.languageSelected
        get() = getBoolean(LANGUAGE_SELECTED, false)
        set(value) {
            editPref {
                it.putBoolean(LANGUAGE_SELECTED, value)
            }
        }

    var SharedPreferences.userMobile
        get() = getString(USER_MOBILE, "")
        set(value) {
            editPref {
                it.putString(USER_MOBILE, value)
            }
        }

    var SharedPreferences.userEmail
        get() = getString(USER_EMAIL, "")
        set(value) {
            editPref {
                it.putString(USER_EMAIL, value)
            }
        }

    var SharedPreferences.userFullName
        get() = getString(USER_FULLNAME, "")
        set(value) {
            editPref {
                it.putString(USER_FULLNAME, value)
            }
        }

    var SharedPreferences.userRegistered
        get() = getBoolean(USER_REGISTERED, false)
        set(value) {
            editPref {
                it.putBoolean(USER_REGISTERED, value)
            }
        }

    var SharedPreferences.sessionToken
        get() = getString(SESSION_TOKEN, "")
        set(value) {
            editPref {
                it.putString(SESSION_TOKEN, value)
            }
        }
}