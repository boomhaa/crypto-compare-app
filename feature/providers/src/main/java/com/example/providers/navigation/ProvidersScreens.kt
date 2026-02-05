package com.example.providers.navigation

sealed class ProvidersScreens(
    val route: String,
) {
    object Providers : ProvidersScreens("providers")
}
