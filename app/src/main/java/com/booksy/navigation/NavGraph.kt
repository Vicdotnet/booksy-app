package com.booksy.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserEntity
import com.booksy.ui.screens.CartScreen
import com.booksy.ui.screens.LoginScreen
import com.booksy.ui.screens.RegisterScreen
import com.booksy.ui.screens.HomeScreen
import com.booksy.ui.screens.ProfileScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    startDestination: String = Screen.Login.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun BooksyApp(appDatabase: AppDatabase) {
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val user = appDatabase.userDao().getUserOnce()
            startDestination = if (user != null) {
                Screen.Home.route
            } else {
                Screen.Login.route
            }
        }
    }

    startDestination?.let { destination ->
        NavGraph(startDestination = destination)
    }
}