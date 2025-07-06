package com.yesvoters.android.database

import com.yesvoters.android.ui.model.response.User
import com.yesvoters.android.utils.AppConstants


object UserPreferences {


    fun saveUserData(user: User) {
        PreferenceUtils.setString(AppConstants.FULL_NAME, user.name)
        PreferenceUtils.setString(AppConstants.EMAIL_ID, user.email)
        user.profile?.let { PreferenceUtils.setString(AppConstants.PROFILE, it) }
        PreferenceUtils.setString(AppConstants.PHONE, user.phone)
    }

    fun saveUserToken(token: String) {
        PreferenceUtils.setString(AppConstants.USER_TOKEN, token)
    }

    fun getFullName(): String {
        return PreferenceUtils.getString(AppConstants.FULL_NAME)
    }

    fun getProfile(): String {
        return PreferenceUtils.getString(AppConstants.PROFILE)
    }

    fun getEmail(): String {
        return PreferenceUtils.getString(AppConstants.EMAIL_ID)
    }

    fun getUserToken(): String {
        return PreferenceUtils.getString(AppConstants.USER_TOKEN)
    }

    fun isUserLoggedIn(): Boolean {
        return getUserToken().isNotEmpty()
    }

    fun logoutUser() {
        PreferenceUtils.remove(AppConstants.FULL_NAME)
        PreferenceUtils.remove(AppConstants.EMAIL_ID)
        PreferenceUtils.remove(AppConstants.USER_TOKEN)
        PreferenceUtils.remove(AppConstants.PROFILE)
        PreferenceUtils.remove(AppConstants.PHONE)
    }
}