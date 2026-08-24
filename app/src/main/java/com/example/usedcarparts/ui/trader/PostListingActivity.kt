package com.example.usedcarparts.ui.trader

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.Listing
import com.example.usedcarparts.databinding.ActivityPostListingBinding
import kotlinx.coroutines.launch

class PostListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostListingBinding
    private val selectedImages = mutableListOf<Uri>()
    private val firebaseRepository = FirebaseRepository()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            updatePhotoContainer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()

        binding.btnAddPhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnPublish.setOnClickListener {
            publishListing()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupDropdowns() {
        val categories = arrayOf("Engine", "Brake", "Body", "Elec.", "Interior", "Other")
        val adapterCat = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.etCategory.setAdapter(adapterCat)

        val conditions = arrayOf("New", "Used", "Refurbished")
        val adapterCond = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, conditions)
        binding.etCondition.setAdapter(adapterCond)
    }

    private fun updatePhotoContainer() {
        // Remove existing preview images (but keep btnAddPhoto)
        val childCount = binding.photoContainer.childCount
        for (i in childCount - 1 downTo 0) {
            val view = binding.photoContainer.getChildAt(i)
            if (view.id != binding.btnAddPhoto.id) {
                binding.photoContainer.removeViewAt(i)
            }
        }

        // Add new images
        selectedImages.forEach { uri ->
            val imageView = ImageView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(240, 240).apply {
                    marginEnd = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
            binding.photoContainer.addView(imageView, 0)
        }
    }

    private fun publishListing() {
        val title = binding.etTitle.text.toString()
        val categoryName = binding.etCategory.text.toString()
        val priceStr = binding.etPrice.text.toString()
        val condition = binding.etCondition.text.toString()
        val makeModel = binding.etMakeModel.text.toString()
        val description = binding.etDescription.text.toString()

        if (title.isEmpty() || categoryName.isEmpty() || priceStr.isEmpty() || condition.isEmpty() || makeModel.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val traderFirebaseId = firebaseRepository.getCurrentUserId() ?: ""
        
        val catId = when(categoryName) {
            "Engine" -> 1
            "Brake" -> 2
            "Body" -> 3
            "Elec." -> 4
            "Interior" -> 5
            "Other" -> 6
            else -> 0
        }

        lifecycleScope.launch {
            binding.btnPublish.isEnabled = false
            Toast.makeText(this@PostListingActivity, "Uploading listing...", Toast.LENGTH_SHORT).show()
            
            val listing = Listing(
                traderFirebaseId = traderFirebaseId,
                title = title,
                categoryId = catId,
                description = description,
                condition = condition,
                price = price,
                carMakeModelYear = makeModel
            )
            
            val success = firebaseRepository.insertListing(listing, selectedImages)
            if (success) {
                Toast.makeText(this@PostListingActivity, "Listing published successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@PostListingActivity, "Failed to publish listing", Toast.LENGTH_SHORT).show()
                binding.btnPublish.isEnabled = true
            }
        }
    }
}
