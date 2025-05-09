package com.example.coursework.presentation.main.navigation

import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.coursework.presentation.auth.*
import com.example.coursework.presentation.files.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController) }
        composable("files") {
            FilesScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}