package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.Listing
import com.example.usedcarparts.data.Order
import com.example.usedcarparts.databinding.ActivityCheckoutBinding
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private val firebaseRepository = FirebaseRepository()
    private var listing: Listing? = null
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityCheckoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            userId = firebaseRepository.getCurrentUserId() ?: ""
            val listingId = intent.getStringExtra("LISTING_FIREBASE_ID") ?: ""
            if (listingId.isNotEmpty()) {
                loadListing(listingId)
            } else {
                // Handle empty cart state
                binding.tvTitle.text = "Empty Cart"
                binding.tvPrice.text = "$0.00"
                binding.tvSubtotal.text = "$0.00"
                binding.tvTotal.text = "$0.00"
                binding.btnPay.isEnabled = false
            }

            binding.btnBack.setOnClickListener { finish() }
            binding.btnPay.setOnClickListener { processPayment() }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun loadListing(listingId: String) {
        lifecycleScope.launch {
            try {
                listing = firebaseRepository.getListingById(listingId)
                listing?.let { l ->
                    binding.tvTitle.text = l.title
                    binding.tvPrice.text = "$${l.price}"
                    binding.tvSubtotal.text = "$${l.price}"
                    val total = l.price + 3.0 // 3.0 delivery fee
                    binding.tvTotal.text = "$${total}"
                } ?: run {
                    android.util.Log.w("CheckoutActivity", "Listing not found: $listingId")
                    Toast.makeText(this@CheckoutActivity, "Item not found in cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("CheckoutActivity", "Error loading listing: $listingId", e)
                Toast.makeText(this@CheckoutActivity, "Error loading cart details", Toast.LENGTH_SHORT).show()
            }
            
            // For now we'll use a placeholder address as migration of all address logic to firestore is large
            binding.tvAddress.text = "House 12, Road 4, Dhaka"
        }
    }

    private fun processPayment() {
        val currentListing = listing ?: run {
            Toast.makeText(this@CheckoutActivity, "No item selected for checkout", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val order = Order(
                    listingFirebaseId = currentListing.firebaseId,
                    buyerFirebaseId = userId,
                    sellerFirebaseId = currentListing.traderFirebaseId,
                    totalAmount = currentListing.price + 3.0,
                    status = "confirmed"
                )
                val orderId = firebaseRepository.placeOrder(order)
                
                if (orderId != null) {
                    // Delete the listing from the marketplace after successful transaction
                    firebaseRepository.deleteListing(currentListing.firebaseId)
                    
                    val intent = Intent(this@CheckoutActivity, OrderConfirmationActivity::class.java)
                    intent.putExtra("ORDER_FIREBASE_ID", orderId)
                    this@CheckoutActivity.startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CheckoutActivity, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("CheckoutActivity", "Error processing payment", e)
                Toast.makeText(this@CheckoutActivity, "An error occurred during payment", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
