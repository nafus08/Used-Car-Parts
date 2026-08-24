package com.example.usedcarparts.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivityLoginBinding
import com.example.usedcarparts.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val firebaseRepository = FirebaseRepository()
    private var selectedRole = "shopper"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.roleToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRole = if (checkedId == binding.btnTrader.id) "trader" else "shopper"
            }
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = firebaseRepository.login(email, password)
            if (result.isSuccess) {
                val userId = result.getOrNull()!!
                val user = firebaseRepository.getUserById(userId)
                if (user != null && user.role == selectedRole) {
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("USER_FIREBASE_ID", userId)
                    intent.putExtra("USER_ROLE", user.role)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Role mismatch or profile not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Invalid credentials"
                Toast.makeText(this@LoginActivity, "Login failed: $error", Toast.LENGTH_LONG).show()
            }
        }
    }
}
