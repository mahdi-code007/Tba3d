package com.example.googlemapstest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.googlemapstest.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.tvPart1.animate().apply {
            duration = 2000
            translationX(300f)
            startDelay = 500
        }

        binding.tvPart2.animate().apply {
            duration = 2000
            translationX(-300f)
            startDelay = 500
        }

        binding.tvPre.alpha = 0f
        binding.tvPre.translationY = 300f
        binding.tvStudentName.alpha = 0f
        binding.tvStudentName.translationY = 350f

        binding.tvPre.animate().apply {
            alpha(1f)
            translationY(0f)
            duration = 2000
            startDelay = 500
        }

        binding.tvStudentName.animate().apply {
            alpha(1f)
            translationY(0f)
            duration = 2000
            startDelay = 500
        }.withEndAction(){
            val intent = Intent(this , MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}