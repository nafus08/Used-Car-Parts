package com.example.usedcarparts.ui.trader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.AppDatabase
import com.example.usedcarparts.data.Listing
import com.example.usedcarparts.databinding.ActivityPostListingBinding
import kotlinx.coroutines.launch

class PostListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostListingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPublish.setOnClickListener {
            publishListing()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun publishListing() {
        val title = binding.etTitle.text.toString()
        val category = binding.etCategory.text.toString()
        val priceStr = binding.etPrice.text.toString()
        val condition = binding.etCondition.text.toString()
        val makeModel = binding.etMakeModel.text.toString()
        val description = binding.etDescription.text.toString()

        if (title.isEmpty() || category.isEmpty() || priceStr.isEmpty() || condition.isEmpty() || makeModel.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val traderId = intent.getIntExtra("USER_ID", -1)

        lifecycleScope.launch {
            try {
                if (traderId == -1) {
                    Toast.makeText(this@PostListingActivity, "Error: Invalid Trader ID. Please log in again.", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                val db = AppDatabase.getDatabase(this@PostListingActivity)
                val listing = Listing(
                    traderId = traderId,
                    title = title,
                    description = description,
                    categoryId = category.toIntOrNull() ?: 1,
                    price = price,
                    condition = condition,
                    carMakeModelYear = makeModel
                )
                db.listingDao().insertListing(listing)
                Toast.makeText(this@PostListingActivity, "Listing published successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PostListingActivity, "Error: ${e.message ?: e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
