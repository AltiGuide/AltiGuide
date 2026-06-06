package com.example.altiguide_mobile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.altiguide_mobile.databinding.ActivitySplashBinding
import com.example.altiguide_mobile.util.AuthDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var authDataStore: AuthDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        // apply splash theme (must match style name in res/values/themes.xml)
        setTheme(R.style.Theme_Altiguidemobile_Splash)
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navigate after 2 seconds safely using coroutines bound to the lifecycle
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000L)
            val token = authDataStore.authTokenFlow.first()
            val destination = if (token.isNotEmpty()) {
                MainActivity::class.java
            } else {
                OnboardingActivity::class.java
            }
            val intent = Intent(this@SplashActivity, destination)
            startActivity(intent)
            finish()
        }
    }
}

