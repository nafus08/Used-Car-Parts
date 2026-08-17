package com.example.usedcarparts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String, // "shopper", "trader"
    val preferredLanguage: String = "en"
)

@Entity(tableName = "traders")
data class Trader(
    @PrimaryKey val userId: Int,
    val businessName: String,
    val businessLicenseNo: String,
    val isVerified: Boolean = false
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val categoryId: Int,
    val nameEn: String,
    val nameAr: String,
    val iconUrl: String? = null
)

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true) val listingId: Int = 0,
    val traderId: Int,
    val categoryId: Int,
    val title: String,
    val description: String,
    val price: Double,
    val condition: String, // "new", "used"
    val carMakeModelYear: String,
    val imageUrl: String? = null,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)
