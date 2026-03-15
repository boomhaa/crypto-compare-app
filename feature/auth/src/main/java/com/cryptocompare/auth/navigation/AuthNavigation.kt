package com.cryptocompare.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryptocompare.auth.ui.screens.loginscreen.LoginScreen
import com.cryptocompare.auth.ui.screens.registerscreen.RegisterScreen
import com.cryptocompare.auth.ui.screens.splashscreen.SplashScreen
import com.cryptocompare.helpers.navigateAndClearStack

@Composable
fun AuthNavigation(onAuthenticated: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthScreens.SplashScreen.route,
    ) {
        composable(AuthScreens.LoginScreen.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(AuthScreens.RegisterScreen.route) },
                onAuthenticated = onAuthenticated,
            )
        }
        composable(AuthScreens.RegisterScreen.route) {
            RegisterScreen(
                onLoginClick = { navController.navigate(AuthScreens.LoginScreen.route) },
                onAuthenticated = onAuthenticated,
            )
        }

        composable(AuthScreens.SplashScreen.route) {
            SplashScreen(
                onNavigateHome = onAuthenticated,
                onNavigateLogin = {
                    navController.navigateAndClearStack(AuthScreens.LoginScreen.route, AuthScreens.SplashScreen.route)
                },
            )
        }
    }
}
