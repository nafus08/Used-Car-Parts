package com.example.usedcarparts.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.AppDatabase
import com.example.usedcarparts.databinding.ActivityPartDetailsBinding
import kotlinx.coroutines.launch

class PartDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPartDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listingId = intent.getIntExtra("LISTING_ID", -1)
        if (listingId != -1) {
            loadListingDetails(listingId)
        }

        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnMessage.setOnClickListener {
            val intent = android.content.Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadListingDetails(listingId: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PartDetailsActivity)
            val listing = db.listingDao().getListingById(listingId)
            
            listing?.let { l ->
                binding.tvTitle.text = l.title
                binding.tvPrice.text = "$${l.price}"
                binding.tvConditionYear.text = "${l.carMakeModelYear} · ${l.condition}"
                binding.tvDescription.text = l.description
                
                val trader = db.userDao().getTraderByUserId(l.traderId)
                trader?.let { t ->
                    binding.tvSellerName.text = t.businessName
                } ?: run {
                    val user = db.userDao().getUserById(l.traderId)
                    binding.tvSellerName.text = user?.fullName ?: "Unknown Seller"
                }
            }
        }
    }
}
