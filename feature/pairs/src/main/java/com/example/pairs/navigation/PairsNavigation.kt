package com.example.pairs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pairs.ui.screens.mainScreen.MainScreen

@Composable
fun PairsNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PairsScreens.MainScreen.route,
    ) {
        composable(route = PairsScreens.MainScreen.route) {
            MainScreen()
        }
    }
}
