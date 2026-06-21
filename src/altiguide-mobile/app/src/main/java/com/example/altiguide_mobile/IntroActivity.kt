package com.example.altiguide_mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.altiguide_mobile.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the fullscreen splash theme
        setTheme(R.style.Theme_Altiguidemobile_Onboarding)
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slides = listOf(
            IntroSlide(
                title = "Basecamp Info",
                description = "Get real-time updates on trail status, camp facilities, and official permit fees from local basecamps.",
                imageResId = R.drawable.img_basecampinfo
            ),
            IntroSlide(
                title = "Offline Maps",
                description = "Access detailed topographic maps and trail routes even without any internet connection.",
                imageResId = R.drawable.img_offlinemaps
            ),
            IntroSlide(
                title = "Track Position",
                description = "Real-time GPS tracking to monitor your exact location and stay safely on the designated trail.",
                imageResId = R.drawable.img_trackposition
            )
        )

        val adapter = IntroAdapter(slides)
        binding.viewPager.adapter = adapter

        // Sync page changes with the indicator views and buttons
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)

                if (position == slides.size - 1) {
                    binding.btnNext.text = "Get Started"
                    binding.btnSkip.visibility = View.INVISIBLE
                } else {
                    binding.btnNext.text = "Next"
                    binding.btnSkip.visibility = View.VISIBLE
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < slides.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishIntro()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishIntro()
        }
    }

    private fun updateIndicators(position: Int) {
        binding.indicatorPage1.setBackgroundResource(
            if (position == 0) R.drawable.indicator_active else R.drawable.indicator_inactive
        )
        binding.indicatorPage2.setBackgroundResource(
            if (position == 1) R.drawable.indicator_active else R.drawable.indicator_inactive
        )
        binding.indicatorPage3.setBackgroundResource(
            if (position == 2) R.drawable.indicator_active else R.drawable.indicator_inactive
        )
    }

    private fun finishIntro() {
        // Proceed to MainActivity (which will handle login / dashboard checking)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
