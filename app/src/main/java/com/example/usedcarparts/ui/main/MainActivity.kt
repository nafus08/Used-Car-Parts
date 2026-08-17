package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import com.example.usedcarparts.data.AppDatabase
import com.example.usedcarparts.databinding.ActivityMainBinding
import com.example.usedcarparts.ui.trader.PostListingActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ListingAdapter
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            adapter = ListingAdapter(emptyList()) { listing ->
                val intent = Intent(this, PartDetailsActivity::class.java)
                intent.putExtra("LISTING_ID", listing.listingId)
                startActivity(intent)
            }
            binding.rvListings.layoutManager = LinearLayoutManager(this)
            binding.rvListings.adapter = adapter

            binding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    loadListings(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            loadListings()

            binding.bottomNavigation.selectedItemId = com.example.usedcarparts.R.id.nav_home
            binding.bottomNavigation.setOnItemSelectedListener { item ->
                binding.searchContainer.visibility = if (item.itemId == com.example.usedcarparts.R.id.nav_search) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                when (item.itemId) {
                    com.example.usedcarparts.R.id.nav_sell -> {
                        val role = intent.getStringExtra("USER_ROLE") ?: "shopper"
                        if (role == "trader") {
                            val traderIntent = Intent(this, PostListingActivity::class.java)
                            traderIntent.putExtra("USER_ID", intent.getIntExtra("USER_ID", 1))
                            startActivity(traderIntent)
                        } else {
                            android.widget.Toast.makeText(this, "Only traders can post listings", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    com.example.usedcarparts.R.id.nav_profile -> {
                        val profileIntent = Intent(this, ProfileActivity::class.java)
                        profileIntent.putExtra("USER_ID", intent.getIntExtra("USER_ID", -1))
                        startActivity(profileIntent)
                        true
                    }
                    com.example.usedcarparts.R.id.nav_chats -> {
                        val chatIntent = Intent(this, ChatListActivity::class.java)
                        startActivity(chatIntent)
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
        loadListings()
    }

    private fun loadListings(query: String = "") {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@MainActivity)
                val flow = if (query.isEmpty()) {
                    db.listingDao().getAllListings()
                } else {
                    db.listingDao().searchListings(query)
                }
                flow.collect { listings ->
                    adapter.updateListings(listings)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
