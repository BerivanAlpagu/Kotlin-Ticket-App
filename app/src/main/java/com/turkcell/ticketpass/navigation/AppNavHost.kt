package com.turkcell.ticketpass.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.ticketpass.screen.LoginScreen
import com.turkcell.ticketpass.screen.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    //authRepository: AuthRepository = koinInject()
)
{
    NavHost(navController=navController, startDestination = Login) {
        composable<Login>{
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {navController.navigate(Register)}
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToLogin = {navController.navigate(Login)}
            )
        }
    }
}