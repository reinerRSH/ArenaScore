package com.example.arena.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.auth.ResetPasswordScreen
import com.example.arena.ui.auth.login.LoginArenaScreen
import com.example.arena.ui.auth.login.LoginViewModel
import com.example.arena.ui.auth.register.RegisterArenaScreen
import com.example.arena.ui.auth.register.RegisterViewModel
import com.example.arena.ui.court.CourtSelectionScreen
import com.example.arena.ui.court.CourtViewModel
import com.example.arena.ui.facility.FacilityListScreen
import com.example.arena.ui.facility.FacilityViewModel
import com.example.arena.ui.home.HomeArenaScreen
import com.example.arena.ui.home.HomeNavigationState
import com.example.arena.ui.home.HomeViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {

        composable<Screen.Login> {
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Login Screen")
            } else {
                val viewModel: LoginViewModel = hiltViewModel()
                LoginArenaScreen(navController = navController, viewModel = viewModel)
            }
        }

        composable<Screen.Register> {
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Register Screen")
            } else {
                val viewModel: RegisterViewModel = hiltViewModel()
                RegisterArenaScreen(navController = navController, viewModel = viewModel)
            }
        }

        composable<Screen.Home> {
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Home Screen")
            } else {
                HomeArenaScreen(navController = navController)
            }
        }

        composable<Screen.FacilityList> { backStackEntry ->
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Facility List Screen")
            } else {
                val route: Screen.FacilityList = backStackEntry.toRoute()
                val sport = route.sport

                // Obtenemos el HomeViewModel asociado a la ruta "Home" para compartir datos del usuario
                val homeBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Home)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(homeBackStackEntry)
                val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value

                val facilityViewModel: FacilityViewModel = hiltViewModel()

                if (homeUiState is HomeNavigationState.Success) {
                    FacilityListScreen(
                        sport = sport,
                        role = homeUiState.role,
                        authorizedSedes = homeUiState.authorizedSedes,
                        viewModel = facilityViewModel,
                        onFacilitySelected = { sede ->
                            navController.navigate(Screen.SelectCourt(sedeId = sede.id))
                        }
                    )
                } else {
                    FacilityListScreen(
                        sport = sport,
                        role = "ATHLETE",
                        authorizedSedes = emptyList(),
                        viewModel = facilityViewModel,
                        onFacilitySelected = { sede ->
                            navController.navigate(Screen.SelectCourt(sedeId = sede.id))
                        }
                    )
                }
            }
        }

        composable<Screen.SelectCourt> { backStackEntry ->
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Select Court Screen")
            } else {
                val route: Screen.SelectCourt = backStackEntry.toRoute()
                val sedeId = route.sedeId
                val courtViewModel: CourtViewModel = hiltViewModel()

                CourtSelectionScreen(
                    sedeId = sedeId,
                    viewModel = courtViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onCourtSelected = { /* TODO: Reservar o ver detalles */ }
                )
            }
        }

        composable<Screen.ForgotPassword> {
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Forgot Password Screen")
            } else {
                ResetPasswordScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}

@Preview(showBackground = true, name = "Navigation - Login")
@Composable
fun LoginPreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Login Screen")
    }
}

@Preview(showBackground = true, name = "Navigation - Register")
@Composable
fun RegisterPreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Register Screen")
    }
}

@Preview(showBackground = true, name = "Navigation - Home")
@Composable
fun HomePreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Home Screen")
    }
}

@Preview(showBackground = true, name = "Navigation - Facility List")
@Composable
fun FacilityListPreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Facility List Screen")
    }
}

@Preview(showBackground = true, name = "Navigation - Select Court")
@Composable
fun SelectCourtPreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Select Court Screen")
    }
}

@Preview(showBackground = true, name = "Navigation - Forgot Password")
@Composable
fun ForgotPasswordPreview() {
    EliteAthleteOSTheme {
        PlaceholderScreen("Forgot Password Screen")
    }
}
