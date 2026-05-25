package com.example.diplom.utils

import android.content.Context

class OnboardingManager(
    context: Context
) {

    private val prefs =

        context.getSharedPreferences(
            "onboarding",
            Context.MODE_PRIVATE
        )

    fun isFirstLaunch(): Boolean {

        return prefs.getBoolean(
            "first_launch",
            true
        )
    }

    fun completeOnboarding() {

        prefs.edit()

            .putBoolean(
                "first_launch",
                false
            )

            .apply()
    }
}