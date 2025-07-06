package com.yesvoters.android.database


object AppSharedPreferences {
    private const val LATITUDE = "latitude"
    private const val LONGITUDE = "longitude"

    fun getLatitude(): String {
        return PreferenceUtils.getString(LATITUDE)
    }

    fun getLongitude(): String {
        return PreferenceUtils.getString(LONGITUDE)
    }

    fun setLongitude(longitude: String) {
        return PreferenceUtils.setString(LONGITUDE, longitude)
    }

    fun setLatitude(latitude: String) {
        return PreferenceUtils.setString(LATITUDE, latitude)
    }
}