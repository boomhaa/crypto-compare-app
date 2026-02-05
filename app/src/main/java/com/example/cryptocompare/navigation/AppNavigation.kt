package com.example.cryptocompare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.navigation.AuthDestination
import com.example.auth.navigation.AuthNavigation
import com.example.auth.navigation.AuthScreens
import com.example.helpers.navigateAndClearStack
import com.example.providers.navigation.ProvidersDestination
import com.example.providers.navigation.ProvidersNavigation
import com.example.providers.navigation.ProvidersScreens

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthDestination.ROUTE,
    ) {
        composable(AuthDestination.ROUTE) {
            AuthNavigation({
                navController.navigateAndClearStack(ProvidersScreens.Providers.route, AuthScreens.SplashScreen.route)
            })
        }

        composable(ProvidersDestination.ROUTE) {
            ProvidersNavigation()
        }
    }
}
