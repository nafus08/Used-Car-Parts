package com.example.usedcarparts.data

data class User(
    val userId: Int = 0, // Kept for logic compatibility
    val firebaseId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val role: String = "shopper", // "shopper", "trader"
    val preferredLanguage: String = "en"
)

data class Trader(
    val userId: Int = 0,
    val firebaseId: String = "",
    val businessName: String = "",
    val businessLicenseNo: String = "",
    val isVerified: Boolean = false
)

data class Category(
    val categoryId: Int = 0,
    val nameEn: String = "",
    val nameAr: String = "",
    val iconUrl: String? = null
)

data class Listing(
    val listingId: Int = 0,
    val firebaseId: String = "",
    val traderId: Int = 0,
    val traderFirebaseId: String = "",
    val categoryId: Int = 0,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val condition: String = "Used", // "New", "Used"
    val carMakeModelYear: String = "",
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)

data class Order(
    val orderId: Int = 0,
    val firebaseId: String = "",
    val listingId: Int = 0,
    val listingFirebaseId: String = "",
    val buyerId: Int = 0,
    val buyerFirebaseId: String = "",
    val sellerId: Int = 0,
    val sellerFirebaseId: String = "",
    val quantity: Int = 1,
    val totalAmount: Double = 0.0,
    val status: String = "pending", // "pending", "confirmed", "shipped", "completed"
    val orderDate: Long = System.currentTimeMillis()
)

data class Message(
    val messageId: Int = 0,
    val firebaseId: String = "",
    val senderId: Int = 0,
    val senderFirebaseId: String = "",
    val receiverId: Int = 0,
    val receiverFirebaseId: String = "",
    val listingId: Int? = null,
    val listingFirebaseId: String? = null,
    val content: String = "",
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class Address(
    val addressId: Int = 0,
    val firebaseId: String = "",
    val userId: Int = 0,
    val userFirebaseId: String = "",
    val addressLine: String = "",
    val city: String = "",
    val country: String = "",
    val isDefault: Boolean = false
)
