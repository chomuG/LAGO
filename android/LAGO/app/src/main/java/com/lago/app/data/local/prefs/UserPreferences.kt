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
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TEMP_TOKEN = "temp_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NICKNAME = "user_nickname"
        private const val KEY_USERNAME = "username"
        private const val KEY_LAST_SELECTED_STOCK = "last_selected_stock"
        private const val KEY_CHART_TIME_FRAME = "chart_time_frame"
        private const val KEY_CHART_INDICATORS = "chart_indicators"
        private const val KEY_INVESTMENT_MODE = "investment_mode" // 0: 모의투자, 1: 역사모드
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

    // 기존 String 버전 메서드들 제거됨 - Long 버전 사용

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

    // 투자 모드 관련 메서드들
    fun getInvestmentMode(): Int {
        return sharedPreferences.getInt(KEY_INVESTMENT_MODE, 0) // 기본값 0 (모의투자)
    }

    fun setInvestmentMode(mode: Int) {
        sharedPreferences.edit()
            .putInt(KEY_INVESTMENT_MODE, mode)
            .apply()
        android.util.Log.d("UserPreferences", "Investment mode saved: $mode")
    }

    // 새로 추가된 토큰 관리 메서드들
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveTempToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_TEMP_TOKEN, token)
            .apply()
    }

    fun getTempToken(): String? {
        return sharedPreferences.getString(KEY_TEMP_TOKEN, null)
    }

    fun clearTempToken() {
        sharedPreferences.edit()
            .remove(KEY_TEMP_TOKEN)
            .apply()
    }

    fun saveUserId(userId: Long) {
        sharedPreferences.edit()
            .putLong(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserIdLong(): Long {
        return sharedPreferences.getLong(KEY_USER_ID, 0L)
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun saveUserNickname(nickname: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_NICKNAME, nickname)
            .apply()
    }

    fun getUserNickname(): String? {
        return sharedPreferences.getString(KEY_USER_NICKNAME, null)
    }

    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TEMP_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NICKNAME)
            .apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}