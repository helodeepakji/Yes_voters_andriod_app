package com.yesvoters.android.database

import com.yesvoters.android.ui.model.response.User


object UserPreferences {

    private const val FULL_NAME = "full_name"
    private const val EMAIL_ID = "email_id"
    private const val USER_TOKEN = "user_token"
    private const val PROFILE = "profile"
    private const val PHONE = "phone"

    fun saveUserData(user: User) {
        PreferenceUtils.setString(FULL_NAME, user.name)
        PreferenceUtils.setString(EMAIL_ID, user.email)
        user.profile?.let { PreferenceUtils.setString(PROFILE, it) }
        PreferenceUtils.setString(PHONE, user.phone)
    }

    fun saveUserToken(token: String) {
        PreferenceUtils.setString(USER_TOKEN, token)
    }

    fun getFullName(): String {
        return PreferenceUtils.getString(FULL_NAME)
    }

    fun getProfile(): String {
        return PreferenceUtils.getString(PROFILE)
    }

    fun getEmail(): String {
        return PreferenceUtils.getString(EMAIL_ID)
    }

    fun getUserToken(): String {
        return PreferenceUtils.getString(USER_TOKEN)
    }

    fun isUserLoggedIn(): Boolean {
        return getUserToken().isNotEmpty()
    }

    fun logoutUser() {
        PreferenceUtils.remove(FULL_NAME)
        PreferenceUtils.remove(EMAIL_ID)
        PreferenceUtils.remove(USER_TOKEN)
        PreferenceUtils.remove(PROFILE)
        PreferenceUtils.remove(PHONE)
    }
}

