package com.example.diplom

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diplom.network.AuthRepository
import com.example.diplom.ui.theme.screens.AuthChoiceScreen
import com.example.diplom.ui.theme.screens.CreateNewPasswordScreen
import com.example.diplom.ui.theme.screens.ForgotPasswordScreen
import com.example.diplom.ui.theme.screens.LoginScreen
import com.example.diplom.ui.theme.screens.MainScreen
import com.example.diplom.ui.theme.screens.OnboardingScreen
import com.example.diplom.ui.theme.screens.OtpVerificationScreen
import com.example.diplom.ui.theme.screens.PasswordChangedScreen
import com.example.diplom.ui.theme.screens.RegistrationScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.diplom.utils.OnboardingManager

private object Routes {
    const val ONBOARDING = "onboarding"
    const val AUTH_CHOICE = "auth_choice"
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val OTP = "otp"
    const val NEW_PASSWORD = "new_password"
    const val PASSWORD_CHANGED = "password_changed"
    const val MAIN = "main"
    const val MAIN_GUEST = "main_guest"
}

@Composable
fun AppNav() {

    val nav =
        rememberNavController()

    val context =
        LocalContext.current

    val onboardingManager =

        remember {

            OnboardingManager(
                context
            )
        }

    val startScreen =

        if(
            onboardingManager
                .isFirstLaunch()
        )

            Routes.ONBOARDING

        else

            Routes.AUTH_CHOICE

    NavHost(
        navController = nav,
        startDestination =
            startScreen
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    onboardingManager
                        .completeOnboarding()
                    nav.navigate(Routes.AUTH_CHOICE) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AUTH_CHOICE) {
            AuthChoiceScreen(
                onRegister = { nav.navigate(Routes.REGISTER) },
                onLogin = { nav.navigate(Routes.LOGIN) },
                onGuestSuccess = {
                    nav.navigate(Routes.MAIN_GUEST) {
                        popUpTo(Routes.AUTH_CHOICE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBack = { nav.popBackStack() },
                onRegister = { _, _, _ ->
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH_CHOICE) { inclusive = true }
                    }
                },
                showRemember = false
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onLogin = { _, _, _ ->
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH_CHOICE) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    nav.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { nav.popBackStack() },
                onContinue = {
                    nav.navigate(Routes.OTP)
                }
            )
        }

        composable(Routes.OTP) {
            OtpVerificationScreen(
                email = "exa****@gmail.com",
                onBack = { nav.popBackStack() },
                onContinue = {
                    nav.navigate(Routes.NEW_PASSWORD)
                }
            )
        }

        composable(Routes.NEW_PASSWORD) {
            CreateNewPasswordScreen(
                onBack = { nav.popBackStack() },
                onContinue = {
                    nav.navigate(Routes.PASSWORD_CHANGED)
                }
            )
        }

        composable(Routes.PASSWORD_CHANGED) {
            PasswordChangedScreen(
                onGoToLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.AUTH_CHOICE) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val context = androidx.compose.ui.platform.LocalContext.current
            MainScreen(
                isGuest = false,
                onLogout = {
                    AuthRepository(context).logout()
                    nav.navigate(Routes.AUTH_CHOICE) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN_GUEST) {
            val context = androidx.compose.ui.platform.LocalContext.current
            MainScreen(
                isGuest = true,
                onLogout = {
                    AuthRepository(context).logout()
                    nav.navigate(Routes.AUTH_CHOICE) {
                        popUpTo(Routes.MAIN_GUEST) { inclusive = true }
                    }
                }
            )
        }
    }
}
