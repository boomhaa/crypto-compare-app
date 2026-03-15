package com.cryptocompare.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryptocompare.auth.navigation.AuthDestination
import com.cryptocompare.auth.navigation.AuthNavigation
import com.cryptocompare.helpers.navigateAndClearStack
import com.cryptocompare.pairs.navigation.PairsDestination
import com.cryptocompare.pairs.navigation.PairsNavigation

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
