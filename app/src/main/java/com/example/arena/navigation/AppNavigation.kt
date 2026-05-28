package com.example.arena.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arena.ui.auth.login.LoginArenaScreen
import com.example.arena.ui.auth.login.LoginViewModel
import com.example.arena.ui.auth.register.RegisterArenaScreen
import com.example.arena.ui.auth.register.RegisterViewModel

@Composable
fun AppNavigation(){

    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ){

        composable(route = Screen.Login.route){

            val viewModel: LoginViewModel = hiltViewModel()


            LoginArenaScreen(navController = navController, viewModel = viewModel)
        }

        composable(route = Screen.Register.route){
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterArenaScreen(navController = navController, viewModel = viewModel)
        }




    }
}