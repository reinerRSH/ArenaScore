package com.example.arena.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arena.View.ui.login.LoginArenaScreen
import com.example.arena.ViewModel.LoginViewModel

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




    }
}