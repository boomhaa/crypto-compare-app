package com.example.providers.navigation

sealed class Screens(
    val route: String,
) {
    object Providers : Screens("providers")
}
