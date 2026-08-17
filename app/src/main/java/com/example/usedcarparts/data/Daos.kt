package com.example.usedcarparts.data

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrader(trader: Trader)

    @Query("SELECT * FROM traders WHERE userId = :userId LIMIT 1")
    suspend fun getTraderByUserId(userId: Int): Trader?

    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updateTrader(trader: Trader)
}

@Dao
interface ListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): kotlinx.coroutines.flow.Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE title LIKE '%' || :query || '%' OR carMakeModelYear LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchListings(query: String): kotlinx.coroutines.flow.Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE listingId = :listingId LIMIT 1")
    suspend fun getListingById(listingId: Int): Listing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}
