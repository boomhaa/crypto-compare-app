package com.example.helpers

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

fun NavHostController.navigateAndClearStack(
    route: String,
    popUpToRoute: String,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(route) {
        popUpTo(popUpToRoute) { inclusive = true }
        launchSingleTop = true
        builder()
    }
}
