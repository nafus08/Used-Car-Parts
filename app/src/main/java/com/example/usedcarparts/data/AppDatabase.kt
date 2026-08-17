package com.example.usedcarparts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Trader::class, Listing::class, Category::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoparts_souq_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                
                // Seed database on background if empty
                CoroutineScope(Dispatchers.IO).launch {
                    instance.seedIfEmpty()
                }
                
                instance
            }
        }
    }

    private suspend fun seedIfEmpty() {
        val listingDao = listingDao()
        val userDao = userDao()
        
        if (listingDao.getAllCategories().isEmpty()) {
            try {
                // Categories
                listingDao.insertCategory(Category(1, "Engine", "المحرك"))
                listingDao.insertCategory(Category(2, "Brake", "الفرامل"))
                listingDao.insertCategory(Category(3, "Body", "الهيكل"))
                listingDao.insertCategory(Category(4, "Elec.", "الكهرباء"))

                userDao.insertUser(User(
                    fullName = "Muntafid Islam",
                    email = "muntafid.islam@gmail.com",
                    phone = "0123456789",
                    passwordHash = "abcdefg",
                    role = "shopper"
                ))
                
                val traderId = userDao.insertUser(User(
                    fullName = "Muntafid Trader",
                    email = "muntafid.trader@gmail.com",
                    phone = "0123456789",
                    passwordHash = "abcdefg",
                    role = "trader"
                )).toInt()
                userDao.insertTrader(Trader(traderId, "Muntafid Parts", "LIC-12345"))
                
                // Sample Listing
                listingDao.insertListing(Listing(
                    traderId = traderId,
                    categoryId = 1,
                    title = "Toyota Corolla Headlight",
                    description = "Original used headlight for 2018 model.",
                    price = 45.0,
                    condition = "Used",
                    carMakeModelYear = "2018 Toyota Corolla"
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
