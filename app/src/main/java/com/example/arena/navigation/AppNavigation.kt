package com.example.arena.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.MainViewModel
import com.example.arena.ui.admin.AdminDashboardScreen
import com.example.arena.ui.admin.AdminDashboardViewModel
import com.example.arena.ui.admin.StaffSetupScreen
import com.example.arena.ui.auth.login.LoginArenaScreen
import com.example.arena.ui.auth.login.LoginViewModel
import com.example.arena.ui.auth.register.RegisterArenaScreen
import com.example.arena.ui.auth.register.RegisterViewModel
import com.example.arena.ui.booking.BookingScheduleScreen
import com.example.arena.ui.booking.BookingScheduleViewModel
import com.example.arena.ui.booking.BookingTypeSelectionScreen
import com.example.arena.ui.booking.BookingTypeViewModel
import com.example.arena.ui.booking.MyReservationsScreen
import com.example.arena.ui.court.CourtSelectionScreen
import com.example.arena.ui.court.CourtViewModel
import com.example.arena.ui.facility.FacilityListScreen
import com.example.arena.ui.facility.FacilityViewModel
import com.example.arena.ui.home.HomeArenaScreen
import com.example.arena.ui.home.HomeNavigationState
import com.example.arena.ui.home.HomeViewModel
import com.example.arena.ui.profile.ProfileScreen
import com.example.arena.ui.gym.GymClassSelectionScreen
import com.example.arena.ui.gym.GymBookingScheduleScreen
import com.example.arena.ui.screens.UserNotificationsSheetShared
import com.example.arena.ui.screens.AdminNotificationsModal
import com.example.arena.ui.screens.AppNotificationsModal
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.arena.R

/**
 * Root Navigation Component.
 * Implements Global Notification System and role-based routing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    
    // Observación reactiva de notificaciones globales
    val isModalOpen by mainViewModel.isNotificationModalOpen.collectAsStateWithLifecycle()
    val isStaffUser by mainViewModel.isStaffUser.collectAsStateWithLifecycle()
    val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()

    // Manejo de navegación desde notificaciones externas (In-app o Push)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val pendingNav by mainViewModel.pendingNavigation.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute, pendingNav) {
        val routeStr = currentRoute?.toString() ?: ""
        val isAuthRoute = routeStr.contains("Login") || routeStr.contains("Register")
        if (pendingNav != null && !isAuthRoute) {
            val (target, sedeId) = pendingNav!!
            
            when (target) {
                "ADMIN_PAYMENTS" -> {
                    val id = sedeId ?: mainViewModel.notifications.value.firstOrNull()?.sedeid
                    if (id != null) {
                        navController.navigate(Screen.AdminDashboard(sedeId = id, initialTab = 4))
                        mainViewModel.consumeNavigation()
                    }
                }
                "USER_RESERVATIONS" -> {
                    navController.navigate(Screen.MyReservations)
                    mainViewModel.consumeNavigation()
                }
            }
        }
    }

    EliteAthleteOSTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AppNavHost(navController = navController, mainViewModel = mainViewModel)
                
                if (isModalOpen) {
                    AppNotificationsModal(
                        notifications = notifications,
                        isStaff = isStaffUser,
                        onDismiss = { mainViewModel.setNotificationModalOpen(false) },
                        onNotificationClick = { reserva ->
                            mainViewModel.handleNotificationClick(reserva) { target ->
                                when (target) {
                                    "ADMIN_PAYMENTS" -> {
                                        navController.navigate(Screen.AdminDashboard(sedeId = reserva.sedeid, initialTab = 4))
                                    }
                                    "USER_RESERVATIONS" -> {
                                        navController.navigate(Screen.MyReservations)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, mainViewModel: MainViewModel) {

    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
        composable<Screen.Login> {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginArenaScreen(navController = navController, viewModel = viewModel)
        }

        composable<Screen.Register> {
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterArenaScreen(navController = navController, viewModel = viewModel)
        }

        composable<Screen.Home> {
            HomeArenaScreen(navController = navController)
        }

        composable<Screen.FacilityList> { backStackEntry ->
            if (LocalInspectionMode.current) {
                PlaceholderScreen("Facility List Screen")
            } else {
                val route: Screen.FacilityList = backStackEntry.toRoute()
                val sport = route.sport

                val homeBackStackEntry = remember(backStackEntry) {
                    navController.getStackEntry(Screen.Home)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(homeBackStackEntry)
                val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
                
                // Usamos las notificaciones del MainViewModel (Global)
                val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()

                val facilityViewModel: FacilityViewModel = hiltViewModel()
                val bookingViewModel: BookingScheduleViewModel = hiltViewModel(homeBackStackEntry)

                FacilityListScreen(
                    sport = sport,
                    role = if (homeUiState is HomeNavigationState.Success) homeUiState.role else "ATHLETE",
                    authorizedSedes = if (homeUiState is HomeNavigationState.Success) homeUiState.authorizedSedes else emptyList(),
                    notifications = notifications,
                    viewModel = facilityViewModel,
                    bookingViewModel = bookingViewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onFacilitySelected = { sede ->
                        if (sport.equals("CROSSFIT", ignoreCase = true) || sport.equals("GYM", ignoreCase = true)) {
                            navController.navigate(Screen.SelectGymClass(sedeId = sede.id, sedeName = sede.nombreSede))
                        } else if (homeUiState is HomeNavigationState.Success && homeUiState.role == "STAFF") {
                            navController.navigate(Screen.AdminDashboard(sedeId = sede.id))
                        } else {
                            navController.navigate(Screen.SelectCourt(sedeId = sede.id))
                        }
                    },
                    onNotificationClick = {
                        // El clic abre el modal global
                        mainViewModel.setNotificationModalOpen(true)
                    },
                    onHistoryClick = {
                        navController.navigate(Screen.MyReservations)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile)
                    }
                )
            }
        }

        composable<Screen.SelectGymClass> { backStackEntry ->
            val route: Screen.SelectGymClass = backStackEntry.toRoute()
            GymClassSelectionScreen(
                sedeId = route.sedeId,
                sedeName = route.sedeName,
                onNavigateBack = { navController.popBackStack() },
                onClassSelected = { gymClass ->
                    navController.navigate(
                        Screen.GymBookingSchedule(
                            sedeId = route.sedeId,
                            classId = gymClass.id,
                            className = gymClass.name,
                            tokenCost = gymClass.tokenCost
                        )
                    )
                }
            )
        }

        composable<Screen.GymBookingSchedule> { backStackEntry ->
            val route: Screen.GymBookingSchedule = backStackEntry.toRoute()
            GymBookingScheduleScreen(
                sedeId = route.sedeId,
                classId = route.classId,
                className = route.className,
                tokenCost = route.tokenCost,
                onNavigateBack = { navController.popBackStack() },
                onBookingConfirmed = { createdReserva ->
                    // Reserva confirmada automáticamente sin validación de pago/admin
                }
            )
        }

        composable<Screen.SelectCourt> { backStackEntry ->
            val route: Screen.SelectCourt = backStackEntry.toRoute()
            val courtViewModel: CourtViewModel = hiltViewModel()
            val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()
            
            CourtSelectionScreen(
                sedeId = route.sedeId,
                viewModel = courtViewModel,
                notifications = notifications,
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { mainViewModel.setNotificationModalOpen(true) },
                onHistoryClick = { navController.navigate(Screen.MyReservations) },
                onCourtSelected = { cancha ->
                    navController.navigate(
                        Screen.BookingType(
                            sedeId = route.sedeId,
                            canchaId = cancha.id,
                            canchaName = cancha.nombre
                        )
                    )
                }
            )
        }

        composable<Screen.BookingType> { backStackEntry ->
            val route: Screen.BookingType = backStackEntry.toRoute()
            val viewModel: BookingTypeViewModel = hiltViewModel()
            val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()

            BookingTypeSelectionScreen(
                sedeId = route.sedeId,
                canchaId = route.canchaId,
                canchaName = route.canchaName,
                viewModel = viewModel,
                notifications = notifications,
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { mainViewModel.setNotificationModalOpen(true) },
                onHistoryClick = { navController.navigate(Screen.MyReservations) },
                onTypeSelected = { type ->
                    navController.navigate(
                        Screen.BookingSchedule(
                            sedeId = route.sedeId,
                            canchaId = route.canchaId,
                            tipoReserva = type,
                            canchaName = route.canchaName
                        )
                    )
                }
            )
        }

        composable<Screen.BookingSchedule> { backStackEntry ->
            val route: Screen.BookingSchedule = backStackEntry.toRoute()
            val viewModel: BookingScheduleViewModel = hiltViewModel()
            val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()

            BookingScheduleScreen(
                sedeId = route.sedeId,
                canchaId = route.canchaId,
                tipoReserva = route.tipoReserva,
                canchaName = route.canchaName,
                viewModel = viewModel,
                notifications = notifications,
                onNavigateBack = { navController.popBackStack() },
                onChangeCourt = { 
                    navController.popBackStack<Screen.SelectCourt>(inclusive = false) 
                },
                onNotificationClick = { mainViewModel.setNotificationModalOpen(true) },
                onHistoryClick = { navController.navigate(Screen.MyReservations) }
            )
        }

        composable<Screen.AdminDashboard> { backStackEntry ->
            val route: Screen.AdminDashboard = backStackEntry.toRoute()
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            AdminDashboardScreen(
                sedeId = route.sedeId,
                initialTab = route.initialTab,
                viewModel = viewModel,
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.StaffSetup> {
            StaffSetupScreen(onSetupComplete = { sedeId ->
                navController.navigate(Screen.AdminDashboard(sedeId)) {
                    popUpTo(Screen.Home) { inclusive = false }
                }
            })
        }

        composable<Screen.MyReservations> {
            val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()
            MyReservationsScreen(
                onNavigateBack = { navController.popBackStack() },
                notifications = notifications,
                onNotificationClick = { mainViewModel.setNotificationModalOpen(true) },
                onHistoryClick = { /* Already here */ }
            )
        }

        composable<Screen.Profile> {
            val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                notifications = notifications,
                onNotificationClick = { mainViewModel.setNotificationModalOpen(true) },
                onHistoryClick = { navController.navigate(Screen.MyReservations) }
            )
        }
    }
}

/**
 * Función para obtener BackStackEntry de forma segura.
 */
private fun NavHostController.getStackEntry(route: Any) = try {
    this.getBackStackEntry(route)
} catch (e: Exception) {
    this.currentBackStackEntry!!
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}
