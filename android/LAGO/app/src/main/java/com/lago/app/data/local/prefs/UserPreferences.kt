package com.lago.app.data.local.prefs

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_LAST_SELECTED_STOCK = "last_selected_stock"
        private const val KEY_CHART_TIME_FRAME = "chart_time_frame"
        private const val KEY_CHART_INDICATORS = "chart_indicators"
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun setAuthToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    fun clearAuthToken() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun setUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun setUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getLastSelectedStock(): String? {
        return sharedPreferences.getString(KEY_LAST_SELECTED_STOCK, null)
    }

    fun setLastSelectedStock(stockCode: String) {
        sharedPreferences.edit()
            .putString(KEY_LAST_SELECTED_STOCK, stockCode)
            .apply()
    }

    fun getChartTimeFrame(): String {
        return sharedPreferences.getString(KEY_CHART_TIME_FRAME, "10") ?: "10"
    }

    fun setChartTimeFrame(timeFrame: String) {
        sharedPreferences.edit()
            .putString(KEY_CHART_TIME_FRAME, timeFrame)
            .apply()
    }

    fun getChartIndicators(): Set<String> {
        return sharedPreferences.getStringSet(KEY_CHART_INDICATORS, setOf("sma5", "sma20", "volume")) ?: setOf()
    }

    fun setChartIndicators(indicators: Set<String>) {
        sharedPreferences.edit()
            .putStringSet(KEY_CHART_INDICATORS, indicators)
            .apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}