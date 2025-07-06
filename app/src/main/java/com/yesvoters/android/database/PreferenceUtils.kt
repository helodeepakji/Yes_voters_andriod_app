package com.yesvoters.android.database

import android.content.Context
import android.content.SharedPreferences
import com.yesvoters.android.R
import com.yesvoters.android.application.AppController

object PreferenceUtils {

    private val sharedPrefs: SharedPreferences by lazy {
        AppController.getGlobalContext()
            .getSharedPreferences(
                AppController.getGlobalContext().getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
    }

    private val editor: SharedPreferences.Editor
        get() = sharedPrefs.edit()

    fun getString(key: String, defValue: String = ""): String {
        return sharedPrefs.getString(key, defValue) ?: defValue
    }

    fun setString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getInt(key: String, defValue: Int = 0): Int {
        return sharedPrefs.getInt(key, defValue)
    }

    fun setInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getBoolean(key: String, defValue: Boolean = false): Boolean {
        return sharedPrefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getFloat(key: String, defValue: Float = 0f): Float {
        return sharedPrefs.getFloat(key, defValue)
    }

    fun setFloat(key: String, value: Float) {
        editor.putFloat(key, value).apply()
    }

    fun getLong(key: String, defValue: Long = 0L): Long {
        return sharedPrefs.getLong(key, defValue)
    }

    fun setLong(key: String, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun getStringList(key: String): List<String> {
        return sharedPrefs.getStringSet(key, emptySet())?.toList() ?: emptyList()
    }

    fun setStringList(key: String, value: List<String>) {
        editor.putStringSet(key, value.toSet()).apply()
    }

    fun remove(key: String) {
        editor.remove(key).apply()
    }

    fun clear() {
        editor.clear().apply()
    }
}
