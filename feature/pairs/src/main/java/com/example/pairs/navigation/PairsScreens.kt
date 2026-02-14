package com.example.pairs.navigation

sealed class PairsScreens(
    val route: String,
) {
    object MainScreen : PairsScreens("main")
}
