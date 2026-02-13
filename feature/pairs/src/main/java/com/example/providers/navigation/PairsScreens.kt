package com.example.providers.navigation

sealed class PairsScreens(
    val route: String,
) {
    object MainScreen : PairsScreens("main")
}
