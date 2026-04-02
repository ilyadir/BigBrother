package com.bigbrother.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val Onboarding = "Onboarding"
    const val Balance = "Balance"
    const val TopUp = "TopUp"
    const val TrackedApps = "TrackedApps"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.Onboarding
    ) {
        composable(Routes.Onboarding) {
            OnboardingScreen()
        }
        composable(Routes.Balance) {
            BalanceScreen()
        }
        composable(Routes.TopUp) {
            TopUpScreen()
        }
        composable(Routes.TrackedApps) {
            TrackedAppsScreen()
        }
    }
}

@Composable
fun OnboardingScreen() = Text(text = "Onboarding")

@Composable
fun BalanceScreen() = Text(text = "Balance")

@Composable
fun TopUpScreen() = Text(text = "TopUp")

@Composable
fun TrackedAppsScreen() = Text(text = "TrackedApps")
