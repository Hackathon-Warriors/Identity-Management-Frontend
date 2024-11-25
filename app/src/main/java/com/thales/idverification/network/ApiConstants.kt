package com.thales.idverification.network

object ApiConstants {

    const val BASE_URL = "https://abd9-163-116-199-118.ngrok-free.app/api/v1/"

    const val VERIFY_USER = "user/verify"
    const val SEND_OTP = "otp/send"
    const val LOGIN_USER = "user/logon"
    const val REGISTER_USER = "user/register"
    const val GET_MOISTURE_DATA = "supply/soil-ground-water"
    const val SURFACE_STORAGE = "supply/surface-storage"
    const val GET_SURFACE_STORAGE_MASTER_DATA = "supply/surface-storage-master"
    const val RAINFALL_DATA = "supply/rainfall"
    const val VILLAGE_WATER_REQUIREMENT_DATA = "demand/water-requirement"
    const val EVAPORATION_BASE_URI = "consumption/evaporation"
    const val OTHER_SOURCES_DATA = "supply/other-storage"
    const val GET_OTHER_SOURCES_MASTER_DATA = "supply/other-storage-master"
    const val GET_CROP_PLAN_MASTER_DATA = "crop/master"
    const val CROP_PLAN_DETAILS = "crop"
    const val GET_SCHEDULES_DATA = "schedules"
    const val GET_DASHBOARD_DATA = "wotr/overview"
    const val MASTER_DATA_URI = "master"
    const val CHECK_LIVELINESS = "liveliness/check"
    const val VERIFY_IDENTITY_DOCUMENT = "docs/poi/match"
    const val CHECK_BANK_STATEMENT = "docs/statement/verify"

}