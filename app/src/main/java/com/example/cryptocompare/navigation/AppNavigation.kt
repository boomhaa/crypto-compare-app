package com.example.cryptocompare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.navigation.AuthDestination
import com.example.auth.navigation.AuthNavigation

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthDestination.ROUTE,
    ) {
        composable(AuthDestination.ROUTE) {
            AuthNavigation()
        }
    }
}
