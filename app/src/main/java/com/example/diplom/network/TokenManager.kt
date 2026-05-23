package com.example.diplom.network

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun clearTokens() {

        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_GUEST)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    fun saveGuestMode(
        isGuest: Boolean
    ) {

        prefs.edit()
            .putBoolean(
                KEY_GUEST,
                isGuest
            )
            .apply()
    }

    fun isGuest(): Boolean {

        return prefs.getBoolean(
            KEY_GUEST,
            false
        )
    }

    fun saveOfflineAccess(
        value:Boolean
    ){

        prefs.edit()
            .putBoolean(
                "offline_access",
                value
            )
            .apply()
    }

    fun canOpenOffline():
            Boolean{

        return prefs.getBoolean(
            "offline_access",
            false
        )
    }

    companion object {

        private const val KEY_ACCESS =
            "access_token"

        private const val KEY_REFRESH =
            "refresh_token"

        private const val KEY_GUEST =
            "guest_mode"
    }
}