package com.example.providers.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.providers.ui.screens.providersScreen.ProvidersScreen

@Composable
fun ProvidersNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProvidersScreens.Providers.route,
    ) {
        composable(route = ProvidersScreens.Providers.route) {
            ProvidersScreen()
        }
    }
}
