package com.example.usedcarparts.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.Trader
import com.example.usedcarparts.data.User
import com.example.usedcarparts.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val firebaseRepository = FirebaseRepository()
    private var currentUser: User? = null
    private var currentTrader: Trader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_FIREBASE_ID") ?: ""
        if (userId.isEmpty()) {
            finish()
            return
        }

        loadData(userId)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun loadData(userId: String) {
        lifecycleScope.launch {
            currentUser = firebaseRepository.getUserById(userId)
            
            currentUser?.let { user ->
                binding.etFullName.setText(user.fullName)
                binding.etPhone.setText(user.phone)
                
                if (user.role == "trader") {
                    binding.traderFields.visibility = View.VISIBLE
                    currentTrader = firebaseRepository.getTraderById(userId)
                    currentTrader?.let { trader ->
                        binding.etBusinessName.setText(trader.businessName)
                        binding.etLicense.setText(trader.businessLicenseNo)
                    }
                }
            }
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString()
        val phone = binding.etPhone.text.toString()

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(fullName = fullName, phone = phone)
                firebaseRepository.updateUser(updatedUser)
                
                if (user.role == "trader") {
                    val businessName = binding.etBusinessName.text.toString()
                    val license = binding.etLicense.text.toString()
                    
                    currentTrader?.let { trader ->
                        val updatedTrader = trader.copy(businessName = businessName, businessLicenseNo = license)
                        firebaseRepository.updateTrader(updatedTrader)
                    } ?: run {
                        // In case trader entry was missing
                        firebaseRepository.updateTrader(Trader(firebaseId = user.firebaseId, businessName = businessName, businessLicenseNo = license))
                    }
                }
                
                Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
