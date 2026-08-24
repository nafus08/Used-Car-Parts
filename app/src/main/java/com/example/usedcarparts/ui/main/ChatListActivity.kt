package com.example.usedcarparts.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.User
import com.example.usedcarparts.databinding.ActivityChatListBinding
import com.example.usedcarparts.databinding.ItemListingBinding
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatAdapter
    private val firebaseRepository = FirebaseRepository()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = firebaseRepository.getCurrentUserId() ?: ""

        adapter = ChatAdapter { partner ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("OTHER_USER_FIREBASE_ID", partner.firebaseId)
            intent.putExtra("OTHER_USER_NAME", partner.fullName)
            startActivity(intent)
        }

        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        if (userId.isNotEmpty()) {
            loadConversations()
        }
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            val partnersIds = firebaseRepository.getConversationPartners(userId)
            val partners = partnersIds.mapNotNull { firebaseRepository.getUserById(it) }
            
            if (partners.isNotEmpty()) {
                binding.emptyView.visibility = android.view.View.GONE
                adapter.updatePartners(partners)
            } else {
                binding.emptyView.visibility = android.view.View.VISIBLE
            }
        }
    }
}

class ChatAdapter(private val onClick: (User) -> Unit) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private var partners = listOf<User>()

    // Reusing ItemListingBinding for simplicity in a mockup, ideally a specific item_chat
    class ViewHolder(val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partner = partners[position]
        holder.binding.tvTitle.text = partner.fullName
        holder.binding.tvDetails.text = "Active conversation"
        holder.binding.tvPrice.visibility = android.view.View.GONE
        holder.binding.btnAddToCart.visibility = android.view.View.GONE
        
        holder.itemView.setOnClickListener { onClick(partner) }
    }

    override fun getItemCount() = partners.size

    fun updatePartners(newPartners: List<User>) {
        partners = newPartners
        notifyDataSetChanged()
    }
}
