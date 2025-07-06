package com.yesvoters.android.database

import com.yesvoters.android.utils.AppConstants


object AppSharedPreferences {

    fun getLatitude(): String {
        return PreferenceUtils.getString(AppConstants.LATITUDE)
    }

    fun getLongitude(): String {
        return PreferenceUtils.getString(AppConstants.LONGITUDE)
    }

    fun setLongitude(longitude: String) {
        return PreferenceUtils.setString(AppConstants.LONGITUDE, longitude)
    }

    fun setLatitude(latitude: String) {
        return PreferenceUtils.setString(AppConstants.LATITUDE, latitude)
    }
}