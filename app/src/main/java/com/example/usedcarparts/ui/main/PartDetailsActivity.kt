package com.example.usedcarparts.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivityPartDetailsBinding
import kotlinx.coroutines.launch

class PartDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPartDetailsBinding
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityPartDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val listingId = intent.getStringExtra("LISTING_FIREBASE_ID") ?: ""
            if (listingId.isNotEmpty()) {
                loadListingDetails(listingId)
            }

            binding.btnBack.setOnClickListener { finish() }
            
            binding.btnMessage.setOnClickListener {
                val traderName = binding.tvSellerName.text.toString()
                val lid = intent.getStringExtra("LISTING_FIREBASE_ID") ?: ""
                
                lifecycleScope.launch {
                    try {
                        val listing = firebaseRepository.getListingById(lid)
                        listing?.let { l ->
                            val intent = android.content.Intent(this@PartDetailsActivity, ChatActivity::class.java)
                            intent.putExtra("OTHER_USER_FIREBASE_ID", l.traderFirebaseId)
                            intent.putExtra("OTHER_USER_NAME", traderName)
                            this@PartDetailsActivity.startActivity(intent)
                        } ?: run {
                            Toast.makeText(this@PartDetailsActivity, "Could not find listing for messaging", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PartDetailsActivity", "Error initiating chat", e)
                    }
                }
            }
            
            binding.btnAddToCart.setOnClickListener {
                val intent = android.content.Intent(this@PartDetailsActivity, CheckoutActivity::class.java)
                intent.putExtra("LISTING_FIREBASE_ID", listingId)
                this@PartDetailsActivity.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun loadListingDetails(listingId: String) {
        lifecycleScope.launch {
            try {
                val listing = firebaseRepository.getListingById(listingId)
                
                listing?.let { l ->
                    binding.tvTitle.text = l.title
                    binding.tvPrice.text = "$${l.price}"
                    binding.tvConditionYear.text = "${l.carMakeModelYear} · ${l.condition}"
                    binding.tvDescription.text = l.description
                    
                    val user = firebaseRepository.getUserById(l.traderFirebaseId)
                    binding.tvSellerName.text = user?.fullName ?: "Unknown Seller"
                } ?: run {
                    android.util.Log.w("PartDetailsActivity", "Listing not found: $listingId")
                    Toast.makeText(this@PartDetailsActivity, "Listing details not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("PartDetailsActivity", "Error loading listing details", e)
            }
        }
    }
}
