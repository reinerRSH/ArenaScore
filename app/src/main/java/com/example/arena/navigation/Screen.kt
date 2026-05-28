package com.example.arena.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable



sealed class Screen (val route: String){

    object Login : Screen("login_Screen")
    object Register : Screen("register_Screen")
    object Home : Screen("home_Screen")
}