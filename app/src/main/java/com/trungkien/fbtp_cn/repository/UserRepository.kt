package com.trungkien.fbtp_cn.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.User
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp as FirebaseTimestamp
import android.util.Log

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "UserRepository"
    }

    fun getCurrentUserProfile(
        onSuccess: (User) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            onError(IllegalStateException("Chưa đăng nhập"))
            return
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                try {
                    val authUser = auth.currentUser
                    val emailFromAuth = authUser?.email
                    val displayNameFromAuth = authUser?.displayName
                    val avatarFromAuth = authUser?.photoUrl?.toString()
                    
                    // Log raw timestamp values for debugging
                    val rawCreatedAt = doc.get("createdAt")
                    val rawUpdatedAt = doc.get("updatedAt")
                    Log.d(TAG, "Raw createdAt: $rawCreatedAt (type: ${rawCreatedAt?.javaClass?.simpleName})")
                    Log.d(TAG, "Raw updatedAt: $rawUpdatedAt (type: ${rawUpdatedAt?.javaClass?.simpleName})")
                    
                    // Safely handle timestamp fields that might have different types
                    val createdAt = when (val createdAtValue = rawCreatedAt) {
                        is Long -> createdAtValue
                        is FirebaseTimestamp -> {
                            Log.d(TAG, "Converting FirebaseTimestamp to Long: ${createdAtValue.seconds * 1000}")
                            createdAtValue.seconds * 1000
                        }
                        is Number -> createdAtValue.toLong()
                        else -> {
                            Log.w(TAG, "Unknown createdAt type: ${createdAtValue?.javaClass?.simpleName}, using current time")
                            System.currentTimeMillis()
                        }
                    }
                    
                    val updatedAt = when (val updatedAtValue = rawUpdatedAt) {
                        is Long -> updatedAtValue
                        is FirebaseTimestamp -> {
                            Log.d(TAG, "Converting FirebaseTimestamp to Long: ${updatedAtValue.seconds * 1000}")
                            updatedAtValue.seconds * 1000
                        }
                        is Number -> updatedAtValue.toLong()
                        else -> {
                            Log.w(TAG, "Unknown updatedAt type: ${updatedAtValue?.javaClass?.simpleName}, using current time")
                            System.currentTimeMillis()
                        }
                    }
                    
                    Log.d(TAG, "Processed createdAt: $createdAt, updatedAt: $updatedAt")
                    
                    val rawAvatar = doc.getString("avatarUrl") ?: ""
                    println("🔄 DEBUG: avatarUrl from Firestore: ${rawAvatar.take(100)}...")
                    println("🔄 DEBUG: avatarUrl length: ${rawAvatar.length}")

                    // Normalize base64 to data URI so AsyncImage can render it
                    val normalizedAvatarUrl = when {
                        rawAvatar.isBlank() -> ""
                        rawAvatar.startsWith("http", ignoreCase = true) -> rawAvatar
                        rawAvatar.startsWith("data:image", ignoreCase = true) -> rawAvatar
                        else -> "data:image/jpeg;base64,$rawAvatar"
                    }
                    
                    val user = User(
                        userId = uid,
                        role = doc.getString("role") ?: "RENTER",
                        name = doc.getString("name")
                            ?: doc.getString("username")
                            ?: displayNameFromAuth
                            ?: (emailFromAuth?.substringBefore('@') ?: ""),
                        email = doc.getString("email") ?: emailFromAuth ?: "",
                        phone = doc.getString("phone") ?: "",
                        avatarUrl = normalizedAvatarUrl,
                        address = doc.getString("address") ?: "",
                        dateOfBirth = doc.getString("dateOfBirth") ?: "",
                        gender = doc.getString("gender") ?: "",
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    onSuccess(user)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating User object", e)
                    onError(e)
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }
    
    fun updateCurrentUserProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null,
        avatarUrl: String? = null,
        onSuccess: (User) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            onError(IllegalStateException("Chưa đăng nhập"))
            return
        }
        val updates = mutableMapOf<String, Any>()
        if (name != null) updates["name"] = name
        if (email != null) updates["email"] = email
        if (phone != null) updates["phone"] = phone
        if (address != null) updates["address"] = address
        if (avatarUrl != null) {
            println("🔄 DEBUG: Updating avatarUrl in Firestore...")
            println("🔄 DEBUG: avatarUrl length: ${avatarUrl.length}")
            println("🔄 DEBUG: avatarUrl first 100 chars: ${avatarUrl.take(100)}")
            updates["avatarUrl"] = avatarUrl
        }
        updates["updatedAt"] = System.currentTimeMillis()
        
        println("🔄 DEBUG: Firestore updates: $updates")
        println("🔄 DEBUG: User ID: $uid")
        
        firestore.collection("users").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                println("✅ DEBUG: Firestore update successful")
                getCurrentUserProfile(onSuccess, onError)
            }
            .addOnFailureListener { e ->
                println("❌ ERROR: Firestore update failed: ${e.message}")
                onError(e)
            }
    }
}


