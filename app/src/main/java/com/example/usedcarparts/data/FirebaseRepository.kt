package com.example.usedcarparts.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
    }

    suspend fun registerUser(user: User, passwordHash: String, trader: Trader?): Result<String> {
        return try {
            android.util.Log.d("FirebaseRepository", "Attempting Auth for email: ${user.email}")
            val result = auth.createUserWithEmailAndPassword(user.email, passwordHash).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Auth success but UID is null"))
            
            android.util.Log.d("FirebaseRepository", "Auth success, UID: $userId. Saving to Firestore...")
            
            val updatedUser = user.copy(firebaseId = userId)
            try {
                firestore.collection("users").document(userId).set(updatedUser).await()
            } catch (e: Exception) {
                android.util.Log.e("FirebaseRepository", "Firestore User save failed", e)
                return Result.failure(Exception("Auth worked, but profile save failed: ${e.message}"))
            }
            
            trader?.let {
                try {
                    firestore.collection("traders").document(userId).set(it.copy(firebaseId = userId)).await()
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseRepository", "Firestore Trader save failed", e)
                    // We don't return failure here yet as user is already created in Auth
                }
            }
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Full Registration Exception", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, passwordHash: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, passwordHash).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Failed to get user ID"))
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Login error", e)
            Result.failure(e)
        }
    }

    fun getAllListings(): Flow<List<Listing>> = callbackFlow {
        val listener = firestore.collection("listings")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val listings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Listing::class.java)?.copy(firebaseId = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseRepository", "Error parsing listing ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertListing(listing: Listing, imageUris: List<Uri>): Boolean {
        return try {
            android.util.Log.d("FirebaseRepository", "Starting listing upload. Images: ${imageUris.size}")
            
            val uploadedUrls = imageUris.map { uri ->
                val fileName = "listings/${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child(fileName)
                android.util.Log.d("FirebaseRepository", "Uploading image to: $fileName")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                android.util.Log.d("FirebaseRepository", "Image uploaded successfully: $url")
                url
            }
            
            val listingWithImages = listing.copy(
                imageUrl = uploadedUrls.firstOrNull(),
                imageUrls = uploadedUrls,
                createdAt = System.currentTimeMillis()
            )
            
            android.util.Log.d("FirebaseRepository", "Saving listing to Firestore...")
            firestore.collection("listings").add(listingWithImages).await()
            android.util.Log.d("FirebaseRepository", "Listing saved successfully!")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Failed to insert listing", e)
            false
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            firestore.collection("users").document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getListingById(listingId: String): Listing? {
        return try {
            firestore.collection("listings").document(listingId).get().await().toObject(Listing::class.java)?.copy(firebaseId = listingId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTraderById(userId: String): Trader? {
        return try {
            firestore.collection("traders").document(userId).get().await().toObject(Trader::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.firebaseId).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTrader(trader: Trader): Boolean {
        return try {
            firestore.collection("traders").document(trader.firebaseId).set(trader).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sendMessage(message: Message): Boolean {
        return try {
            firestore.collection("messages").add(message).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getMessages(userId: String, otherId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("messages")
            .orderBy("sentAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(firebaseId = doc.id)
                } ?: emptyList()
                
                // Filter locally to avoid complex composite index requirement in Firestore for simple queries
                val filtered = messages.filter { 
                    (it.senderFirebaseId == userId && it.receiverFirebaseId == otherId) || 
                    (it.senderFirebaseId == otherId && it.receiverFirebaseId == userId) 
                }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    suspend fun placeOrder(order: Order): String? {
        return try {
            val doc = firestore.collection("orders").add(order).await()
            doc.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            firestore.collection("orders").document(orderId).get().await().toObject(Order::class.java)?.copy(firebaseId = orderId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getConversationPartners(userId: String): List<String> {
        return try {
            val sent = firestore.collection("messages").whereEqualTo("senderFirebaseId", userId).get().await()
            val received = firestore.collection("messages").whereEqualTo("receiverFirebaseId", userId).get().await()
            
            val partners = mutableSetOf<String>()
            sent.toObjects(Message::class.java).forEach { partners.add(it.receiverFirebaseId) }
            received.toObjects(Message::class.java).forEach { partners.add(it.senderFirebaseId) }
            partners.toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteListing(listingId: String): Boolean {
        return try {
            firestore.collection("listings").document(listingId).delete().await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error deleting listing $listingId", e)
            false
        }
    }
}
