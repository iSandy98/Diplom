package com.example.diplom.network

import android.content.Context
import java.util.UUID

class AuthRepository(
    private val context: Context
) {
    private val api: ApiService = RetrofitInstance.create(context)
    private val tokenManager = TokenManager(context)

    suspend fun register(
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return runCatching {
            val response = api.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    password_confirm = password
                )
            )
            tokenManager.saveTokens(
                access = response.access,
                refresh = response.refresh
            )
            tokenManager
                .saveOfflineAccess(
                    true
                )
            response
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<TokenResponse> {
        return runCatching {
            val response = api.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )
            tokenManager.saveTokens(
                access = response.access,
                refresh = response.refresh
            )
            tokenManager
                .saveOfflineAccess(
                    true
                )
            tokenManager.saveGuestMode(
                false
            )
            response
        }
    }

    suspend fun loginAsGuest(): Result<GuestResponse> {
        return runCatching {
            val response = api.guestLogin(
                GuestRequest(
                    device_id = UUID.randomUUID().toString()
                )
            )
            tokenManager.saveTokens(
                access = response.access,
                refresh = response.refresh
            )
            tokenManager
                .saveOfflineAccess(
                    true
                )
            tokenManager.saveGuestMode(true)
            response
        }
    }

    suspend fun refreshToken(): Result<TokenResponse> {
        return runCatching {
            val refresh = tokenManager.getRefreshToken()
                ?: error("Refresh token not found")

            val response = api.refreshToken(
                RefreshTokenRequest(refresh = refresh)
            )

            tokenManager.saveTokens(
                access = response.access,
                refresh = response.refresh
            )
            response
        }
    }

    suspend fun getProfile(): Result<ProfileDto> {
        return runCatching {
            api.getProfile()
        }
    }

    fun getAccessToken(): String? = tokenManager.getAccessToken()

    fun getRefreshToken(): String? = tokenManager.getRefreshToken()

    fun logout() {
        tokenManager.clearTokens()
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun isGuest(): Boolean =
        tokenManager.isGuest()
}