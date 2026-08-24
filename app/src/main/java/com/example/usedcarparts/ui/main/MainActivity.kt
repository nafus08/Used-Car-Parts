package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.databinding.ActivityMainBinding
import com.example.usedcarparts.ui.trader.PostListingActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ListingAdapter
    private val firebaseRepository = FirebaseRepository()
    
    private val currentQuery = MutableStateFlow("")
    private val currentCategoryId = MutableStateFlow<Int?>(null)
    private var lastAddedListingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val currentUserId = firebaseRepository.getCurrentUserId()

            adapter = ListingAdapter(
                listings = emptyList(),
                currentUserId = currentUserId,
                onListingClick = { listing ->
                    val intent = Intent(this@MainActivity, PartDetailsActivity::class.java)
                    intent.putExtra("LISTING_FIREBASE_ID", listing.firebaseId)
                    this@MainActivity.startActivity(intent)
                },
                onDeleteClick = { listing ->
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete Listing")
                        .setMessage("Are you sure you want to delete this listing?")
                        .setPositiveButton("Delete") { _, _ ->
                            lifecycleScope.launch {
                                val success = firebaseRepository.deleteListing(listing.firebaseId)
                                if (success) {
                                    android.widget.Toast.makeText(this@MainActivity, "Listing deleted", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(this@MainActivity, "Failed to delete", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                },
                onAddToCartClick = { listing ->
                    lastAddedListingId = listing.firebaseId
                    android.widget.Toast.makeText(this@MainActivity, "Added to Cart!", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            binding.rvListings.layoutManager = LinearLayoutManager(this)
            binding.rvListings.adapter = adapter

            binding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentQuery.value = s?.toString() ?: ""
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.catEngine.setOnClickListener { filterByCategory(1) }
            binding.catBrake.setOnClickListener { filterByCategory(2) }
            binding.catBody.setOnClickListener { filterByCategory(3) }
            binding.catElec.setOnClickListener { filterByCategory(4) }
            binding.catInterior.setOnClickListener { filterByCategory(5) }
            binding.catOther.setOnClickListener { filterByCategory(6) }

            observeListings()

            val role = intent.getStringExtra("USER_ROLE") ?: "shopper"
            if (role == "shopper") {
                val menu = binding.bottomNavigation.menu
                val sellItem = menu.findItem(com.example.usedcarparts.R.id.nav_sell)
                sellItem?.let {
                    it.title = getString(com.example.usedcarparts.R.string.nav_cart)
                    it.icon = androidx.core.content.ContextCompat.getDrawable(this, android.R.drawable.ic_menu_agenda)
                }
            }

            binding.bottomNavigation.selectedItemId = com.example.usedcarparts.R.id.nav_home
            binding.bottomNavigation.setOnItemSelectedListener { item ->
                binding.searchContainer.visibility = if (item.itemId == com.example.usedcarparts.R.id.nav_search) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                when (item.itemId) {
                    com.example.usedcarparts.R.id.nav_sell -> {
                        if (role == "trader") {
                            val traderIntent = Intent(this@MainActivity, PostListingActivity::class.java)
                            traderIntent.putExtra("USER_FIREBASE_ID", firebaseRepository.getCurrentUserId())
                            this@MainActivity.startActivity(traderIntent)
                        } else {
                            // Open Cart or Checkout
                            val checkoutIntent = Intent(this@MainActivity, CheckoutActivity::class.java)
                            checkoutIntent.putExtra("LISTING_FIREBASE_ID", lastAddedListingId)
                            this@MainActivity.startActivity(checkoutIntent)
                        }
                        true
                    }
                    com.example.usedcarparts.R.id.nav_profile -> {
                        val profileIntent = Intent(this@MainActivity, ProfileActivity::class.java)
                        profileIntent.putExtra("USER_FIREBASE_ID", firebaseRepository.getCurrentUserId())
                        this@MainActivity.startActivity(profileIntent)
                        true
                    }
                    com.example.usedcarparts.R.id.nav_chats -> {
                        val chatIntent = Intent(this@MainActivity, ChatListActivity::class.java)
                        this@MainActivity.startActivity(chatIntent)
                        true
                    }
                    else -> true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun filterByCategory(id: Int) {
        currentCategoryId.value = if (currentCategoryId.value == id) null else id
        // Visual feedback for selected category
        binding.catEngine.alpha = if (currentCategoryId.value == 1) 1.0f else 0.6f
        binding.catBrake.alpha = if (currentCategoryId.value == 2) 1.0f else 0.6f
        binding.catBody.alpha = if (currentCategoryId.value == 3) 1.0f else 0.6f
        binding.catElec.alpha = if (currentCategoryId.value == 4) 1.0f else 0.6f
        binding.catInterior.alpha = if (currentCategoryId.value == 5) 1.0f else 0.6f
        binding.catOther.alpha = if (currentCategoryId.value == 6) 1.0f else 0.6f
        
        android.widget.Toast.makeText(this, "Filter: ${if (currentCategoryId.value == null) "All" else id}", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun observeListings() {
        lifecycleScope.launch {
            try {
                combine(
                    firebaseRepository.getAllListings().catch { e ->
                        android.util.Log.e("MainActivity", "Error in getAllListings flow", e)
                        emit(emptyList())
                    },
                    currentCategoryId,
                    currentQuery
                ) { listings, catId, query ->
                    android.util.Log.d("MainActivity", "Filtering ${listings.size} listings by Cat: $catId, Query: $query")
                    listings.filter { 
                        val matchesCategory = if (catId != null) it.categoryId == catId else true
                        val matchesQuery = if (query.isNotEmpty()) it.title.contains(query, ignoreCase = true) else true
                        matchesCategory && matchesQuery
                    }
                }.collectLatest { filtered ->
                    android.util.Log.d("MainActivity", "Filtered result: ${filtered.size} items")
                    adapter.updateListings(filtered)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error observing listings", e)
            }
        }
    }
}
