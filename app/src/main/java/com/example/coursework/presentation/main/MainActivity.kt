package com.example.coursework.presentation.main

import android.content.*
import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.coursework.presentation.main.navigation.*
import com.example.coursework.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showExitDialog by remember { mutableStateOf(false) }

            BackHandler {
                showExitDialog = true
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Выход из приложения") },
                    text = { Text("Вы точно хотите выйти?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            finishAffinity()
                        }) {
                            Text("Да")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                        }) {
                            Text("Нет")
                        }
                    }
                )
            }

            CourseWorkTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}