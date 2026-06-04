package com.turkcell.ticketpass.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.AuthState
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.ticketpass.screen.CheckinScreen
import com.turkcell.ticketpass.screen.EventDetailScreen
import com.turkcell.ticketpass.screen.HomeScreen
import com.turkcell.ticketpass.screen.LoginScreen
import com.turkcell.ticketpass.screen.MyPurchasesScreen
import com.turkcell.ticketpass.screen.MyTicketsScreen
import com.turkcell.ticketpass.screen.RegisterScreen
import com.turkcell.ticketpass.screen.TicketDetailScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
) {
    val authState by authRepository.authState.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthState.Loading -> SplashScreen()
        is AuthState.Unauthenticated -> UnAuthedNavHost(navController)
        is AuthState.Authenticated -> {
            when (state.user.role) {
                UserRole.STAFF, UserRole.ADMIN -> StaffNavHost(navController)
                UserRole.USER -> UserNavHost(navController)
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}


@Composable
private fun UserNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(EventDetail(eventId = eventId))
                },
                onMyTicketsClick = {
                    navController.navigate(MyTickets)
                },
                onMyPurchasesClick = {
                    navController.navigate(MyPurchases)
                },
                onCheckinClick = {}
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMyTickets = {
                    navController.navigate(MyTickets) {
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<MyTickets> {
            MyTicketsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTicketClick = { ticketId ->
                    navController.navigate(TicketDetail(ticketId = ticketId))
                }
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<MyPurchases> {
            MyPurchasesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun StaffNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Checkin) {
        composable<Checkin> {
            CheckinScreen()
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) }
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToLogin = { navController.navigate(Login) }
            )
        }
    }
}
