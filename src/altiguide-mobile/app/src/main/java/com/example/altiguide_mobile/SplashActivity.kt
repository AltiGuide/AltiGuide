package com.example.altiguide_mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.altiguide_mobile.databinding.ActivityOnboardingBinding
import com.example.altiguide_mobile.util.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // apply splash theme (must match style name in res/values/themes.xml)
        setTheme(R.style.Theme_Altiguidemobile_Splash)
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the "Start Journey" button to make it look like a pure splash screen
        binding.btnStartJourney.visibility = View.GONE

        val authDataStore = AuthDataStore(applicationContext)

        // navigate after 2 seconds safely using coroutines bound to the lifecycle
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000L)
            
            val token = try {
                authDataStore.authTokenFlow.first()
            } catch (e: Exception) {
                ""
            }

            val destination = if (token.isNotEmpty()) {
                MainActivity::class.java
            } else {
                OnboardingActivity::class.java
            }
            val intent = Intent(this@SplashActivity, destination)
            startActivity(intent)
            // overridePendingTransition(0, 0)
            finish()
        }
    }
}


