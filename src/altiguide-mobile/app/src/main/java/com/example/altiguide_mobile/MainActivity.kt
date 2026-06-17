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
            val tokenState = authDataStore.authTokenFlow.collectAsState(initial = null)
            val scope = rememberCoroutineScope()

            AltiguidemobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        when (val token = tokenState.value) {
                            null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            "" -> {
                                LoginScreen()
                            }
                            else -> {
                                HomeScreen(
                                    userName = "Diva",
                                    onLogout = {
                                        scope.launch {
                                            authDataStore.clearToken()
                                            this@MainActivity.recreate()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}