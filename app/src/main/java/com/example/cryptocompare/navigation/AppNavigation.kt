package com.example.cryptocompare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.providers.navigation.ProvidersDestination
import com.example.providers.navigation.ProvidersNavigation

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProvidersDestination.ROUTE,
    ) {
        composable(ProvidersDestination.ROUTE) {
            ProvidersNavigation()
        }
    }
}
