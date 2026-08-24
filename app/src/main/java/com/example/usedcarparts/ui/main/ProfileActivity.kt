package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivityProfileBinding
import com.example.usedcarparts.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_FIREBASE_ID") ?: ""

        if (userId.isNotEmpty()) {
            loadUserProfile(userId)
        }

        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            intent.putExtra("USER_FIREBASE_ID", userId)
            startActivityForResult(intent, 1001)
        }
        
        binding.btnLogout.setOnClickListener {
            firebaseRepository.logout()
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val userId = intent.getStringExtra("USER_FIREBASE_ID") ?: ""
            if (userId.isNotEmpty()) {
                loadUserProfile(userId)
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        lifecycleScope.launch {
            val user = firebaseRepository.getUserById(userId)
            
            user?.let {
                binding.tvUserName.text = it.fullName
                binding.tvUserRole.text = it.role
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone
                
                // For simplicity, we assume trader info is in a "traders" collection with same ID
                if (it.role == "trader") {
                    // Need a getTraderById in FirebaseRepository
                    val trader = firebaseRepository.getTraderById(userId)
                    trader?.let { t ->
                        binding.traderDetails.visibility = View.VISIBLE
                        binding.tvBusinessName.text = t.businessName
                        binding.tvLicense.text = t.businessLicenseNo
                    }
                }
            }
        }
    }
}
