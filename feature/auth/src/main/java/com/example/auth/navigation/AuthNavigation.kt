package com.example.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.ui.screens.loginscreen.LoginScreen
import com.example.auth.ui.screens.registerscreen.RegisterScreen

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthScreens.LoginScreen.route,
    ) {
        composable(AuthScreens.LoginScreen.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(AuthScreens.RegisterScreen.route) },
            )
        }
        composable(AuthScreens.RegisterScreen.route) {
            RegisterScreen(
                onLoginClick = { navController.navigate(AuthScreens.LoginScreen.route) },
            )
        }
    }
}
