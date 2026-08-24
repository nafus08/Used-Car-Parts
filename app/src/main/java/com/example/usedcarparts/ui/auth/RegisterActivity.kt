package com.example.usedcarparts.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.Trader
import com.example.usedcarparts.data.User
import com.example.usedcarparts.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val firebaseRepository = FirebaseRepository()
    private var selectedRole = "shopper"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.roleToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRole = if (checkedId == binding.btnTrader.id) "trader" else "shopper"
                binding.tilBusinessName.visibility = if (selectedRole == "trader") View.VISIBLE else View.GONE
                binding.tilLicense.visibility = if (selectedRole == "trader") View.VISIBLE else View.GONE
            }
        }

        binding.btnCreateAccount.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()
        val password = binding.etPassword.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to terms", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = User(
                fullName = fullName,
                email = email,
                phone = phone,
                role = selectedRole
            )

            val trader = if (selectedRole == "trader") {
                val businessName = binding.etBusinessName.text.toString()
                val license = binding.etLicense.text.toString()
                Trader(businessName = businessName, businessLicenseNo = license)
            } else null

            val result = firebaseRepository.registerUser(user, password, trader)

            if (result.isSuccess) {
                Toast.makeText(this@RegisterActivity, "Success! Redirecting to login...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown Error"
                android.app.AlertDialog.Builder(this@RegisterActivity)
                    .setTitle("Registration Failed")
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}
