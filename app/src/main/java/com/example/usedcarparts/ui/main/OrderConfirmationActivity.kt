package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivityOrderConfirmationBinding
import kotlinx.coroutines.launch

class OrderConfirmationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderConfirmationBinding
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getStringExtra("ORDER_FIREBASE_ID") ?: ""
        if (orderId.isNotEmpty()) {
            loadOrderDetails(orderId)
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this@OrderConfirmationActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            this@OrderConfirmationActivity.startActivity(intent)
        }
        
        binding.btnTrack.setOnClickListener {
            // Track functionality placeholder
        }
    }

    private fun loadOrderDetails(orderId: String) {
        lifecycleScope.launch {
            try {
                val order = firebaseRepository.getOrderById(orderId)
                order?.let { o ->
                    val displayId = if (o.firebaseId.length >= 5) o.firebaseId.takeLast(5) else o.firebaseId
                    binding.tvOrderId.text = "Order #AP-${displayId.uppercase()} · $${o.totalAmount}"
                    
                    val listing = firebaseRepository.getListingById(o.listingFirebaseId)
                    listing?.let { l ->
                        binding.tvItemName.text = l.title
                    }
                    
                    val trader = firebaseRepository.getTraderById(o.sellerFirebaseId)
                    binding.tvSellerName.text = trader?.businessName ?: "Seller"
                    
                    binding.tvPaymentMethod.text = "Card Payment" // Placeholder
                    binding.tvStatus.text = o.status.replaceFirstChar { it.uppercase() }
                }
            } catch (e: Exception) {
                android.util.Log.e("OrderConfirmationActivity", "Error loading order details", e)
            }
        }
    }
}
