package com.example.altiguide_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.altiguide_mobile.ui.theme.AltiguidemobileTheme
import com.example.altiguide_mobile.ui.auth.LoginScreen
import com.example.altiguide_mobile.ui.home.HomeScreen
import com.example.altiguide_mobile.util.AuthDataStore
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authDataStore = AuthDataStore(applicationContext)
        setContent {
            AltiguidemobileTheme {
                val token by authDataStore.authTokenFlow.collectAsState(initial = null)
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    composable("login") {
                        LoginScreen()
                    }
                    composable("main") {
                        HomeScreen(
                            onLogout = {
                                scope.launch {
                                    authDataStore.clearToken()
                                }
                            }
                        )
                    }
                }

                LaunchedEffect(token) {
                    when (token) {
                        null -> {} // stay on splash
                        "" -> {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
