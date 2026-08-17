package com.example.usedcarparts.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.AppDatabase
import com.example.usedcarparts.databinding.ActivityLoginBinding
import com.example.usedcarparts.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
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
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Hardcoded account bypass
        if (email == "muntafid.islam@gmail.com" && password == "abcdefg") {
            try {
                Toast.makeText(this, "Logged in as Admin ($selectedRole)", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_ID", 1) // Standard ID for the first user
                intent.putExtra("USER_ROLE", selectedRole)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error starting MainActivity: ${e.message}", Toast.LENGTH_LONG).show()
            }
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@LoginActivity)
            val user = db.userDao().getUserByEmail(email)

            if (user != null && user.passwordHash == password && user.role == selectedRole) {
                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("USER_ID", user.userId)
                intent.putExtra("USER_ROLE", user.role)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Invalid credentials or role", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
