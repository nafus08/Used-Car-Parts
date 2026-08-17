package com.example.usedcarparts.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.usedcarparts.databinding.ActivitySplashBinding
import com.example.usedcarparts.ui.auth.LoginActivity

import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnglish.setOnClickListener {
            setLocale("en")
        }

        binding.btnArabic.setOnClickListener {
            setLocale("ar")
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        // This is a basic locale switch. For persistent changes, we'd use a ContextWrapper
        // or recreate the activity. For now, we direct to Login with the new config.
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
