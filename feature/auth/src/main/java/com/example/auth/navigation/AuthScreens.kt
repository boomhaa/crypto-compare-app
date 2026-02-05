package com.example.auth.navigation

sealed class AuthScreens(
    val route: String,
) {
    object LoginScreen : AuthScreens("login")

    object RegisterScreen : AuthScreens("register")

    object SplashScreen : AuthScreens("splash")
}
