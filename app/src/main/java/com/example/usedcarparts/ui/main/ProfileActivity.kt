package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.AppDatabase
import com.example.usedcarparts.databinding.ActivityProfileBinding
import com.example.usedcarparts.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId != -1) {
            loadUserProfile(userId)
        }

        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivityForResult(intent, 1001)
        }
        
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val userId = intent.getIntExtra("USER_ID", -1)
            if (userId != -1) {
                loadUserProfile(userId)
            }
        }
    }

    private fun loadUserProfile(userId: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ProfileActivity)
            val user = db.userDao().getUserById(userId)
            
            user?.let {
                binding.tvUserName.text = it.fullName
                binding.tvUserRole.text = it.role
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone
                
                if (it.role == "trader") {
                    val trader = db.userDao().getTraderByUserId(it.userId)
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
