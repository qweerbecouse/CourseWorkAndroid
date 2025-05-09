package com.example.coursework.presentation.auth

import android.content.*
import android.net.*
import android.widget.*
import androidx.activity.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import com.example.coursework.R
import com.example.coursework.data.remote.*
import com.example.coursework.data.storage.*
import kotlinx.coroutines.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenStore = remember { TokenDataStore(context) }
    val allTokens by tokenStore.allTokens.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val uri = (context as? ComponentActivity)?.intent?.data
        val tokenFromUri = uri?.fragment
            ?.split("&")
            ?.firstOrNull { it.startsWith("access_token=") }
            ?.substringAfter("=")

        if (!tokenFromUri.isNullOrEmpty()) {
            tokenStore.addToken(tokenFromUri)
            tokenStore.selectToken(tokenFromUri)

            try {
                val api = ApiClient.createUserInfoApi()
                val name = api.getUserInfo("OAuth $tokenFromUri").login
                tokenStore.saveTokenName(tokenFromUri, name)
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось получить имя", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(context, "Вход выполнен", Toast.LENGTH_SHORT).show()
            navController.navigate("files") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFBB86FC), Color(0xFFEDE7F6))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Добро пожаловать 👋", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    allTokens.forEach { token ->
                        val name by tokenStore.getTokenName(token).collectAsState(initial = null)

                        Button(
                            onClick = {
                                scope.launch {
                                    tokenStore.selectToken(token)
                                    navController.navigate("files") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Войти как ${name ?: "[неизвестно]"}")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val authUri = Uri.parse(
                                "https://oauth.yandex.ru/authorize" +
                                        "?response_type=token" +
                                        "&client_id=8ffb6719654e4769891f9476f63cd9c6" +
                                        "&redirect_uri=com.example.coursework://oauth" +
                                        "&force_confirm=yes"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, authUri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить аккаунт")
                    }
                }
            }
        }
    }
}