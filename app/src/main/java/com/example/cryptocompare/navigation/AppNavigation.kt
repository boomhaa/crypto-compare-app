package com.example.cryptocompare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.navigation.AuthDestination
import com.example.auth.navigation.AuthNavigation
import com.example.helpers.navigateAndClearStack
import com.example.providers.navigation.PairsDestination
import com.example.providers.navigation.PairsNavigation

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthDestination.ROUTE,
    ) {
        composable(AuthDestination.ROUTE) {
            AuthNavigation {
                navController.navigateAndClearStack(PairsDestination.ROUTE, AuthDestination.ROUTE)
            }
        }

        composable(PairsDestination.ROUTE) {
            PairsNavigation()
        }
    }
}
