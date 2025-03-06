package com.studyedge.phie

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.studyedge.phie.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val splashText = "StudyEdge"
    private var currentIndex = 0
    private val typingDelay = 100L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        val scaleX = ObjectAnimator.ofFloat(binding.splashLogo, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.splashLogo, "scaleY", 0.5f, 1f)
        scaleX.duration = 1000
        scaleY.duration = 1000
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        scaleX.start()
        scaleY.start()

        // Start typing animation
        startTypingAnimation()
    }

    private fun startTypingAnimation() {
        if (currentIndex <= splashText.length) {
            binding.splashText.text = splashText.substring(0, currentIndex)
            currentIndex++
            Handler(Looper.getMainLooper()).postDelayed({
                startTypingAnimation()
            }, typingDelay)
        } else {
            // Animation complete, navigate to main activity after delay
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 500)
        }
    }
} 