package com.example.usedcarparts.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.usedcarparts.data.FirebaseRepository
import com.example.usedcarparts.data.Message
import com.example.usedcarparts.databinding.ActivityChatBinding
import com.example.usedcarparts.databinding.ItemMessageReceivedBinding
import com.example.usedcarparts.databinding.ItemMessageSentBinding
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private val firebaseRepository = FirebaseRepository()
    private var userId: String = ""
    private var otherId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = firebaseRepository.getCurrentUserId() ?: ""
        otherId = intent.getStringExtra("OTHER_USER_FIREBASE_ID") ?: ""
        val otherName = intent.getStringExtra("OTHER_USER_NAME") ?: "User"
        binding.tvOtherUserName.text = otherName

        if (userId.isEmpty() || otherId.isEmpty()) {
            finish()
            return
        }

        adapter = MessageAdapter(userId)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        loadMessages()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendMessage() }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            firebaseRepository.getMessages(userId, otherId).collect { messages ->
                adapter.updateMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun sendMessage() {
        val content = binding.etMessage.text.toString()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            val message = Message(
                senderFirebaseId = userId,
                receiverFirebaseId = otherId,
                content = content
            )
            firebaseRepository.sendMessage(message)
            binding.etMessage.text.clear()
        }
    }
}

class MessageAdapter(private val currentUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = listOf<Message>()

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderFirebaseId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.binding.tvMessage.text = message.content
        else if (holder is ReceivedViewHolder) holder.binding.tvMessage.text = message.content
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
}
