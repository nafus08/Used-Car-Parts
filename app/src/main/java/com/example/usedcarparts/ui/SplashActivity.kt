package com.example.usedcarparts.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivitySplashBinding
import com.example.usedcarparts.ui.auth.LoginActivity
import com.example.usedcarparts.ui.main.MainActivity
import kotlinx.coroutines.launch

import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is already logged in
        val currentUserId = firebaseRepository.getCurrentUserId()
        if (currentUserId != null) {
            autoLogin(currentUserId)
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnglish.setOnClickListener {
            setLocale("en")
        }

        binding.btnArabic.setOnClickListener {
            setLocale("ar")
        }
    }

    private fun autoLogin(userId: String) {
        lifecycleScope.launch {
            val user = firebaseRepository.getUserById(userId)
            if (user != null) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.putExtra("USER_FIREBASE_ID", userId)
                intent.putExtra("USER_ROLE", user.role)
                startActivity(intent)
                finish()
            }
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
