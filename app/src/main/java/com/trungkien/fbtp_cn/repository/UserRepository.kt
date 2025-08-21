package com.trungkien.fbtp_cn.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.User
import com.google.firebase.firestore.SetOptions

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
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
                val authUser = auth.currentUser
                val emailFromAuth = authUser?.email
                val displayNameFromAuth = authUser?.displayName
                val avatarFromAuth = authUser?.photoUrl?.toString()
                val user = User(
                    id = uid,
                    name = doc.getString("name")
                        ?: doc.getString("username")
                        ?: displayNameFromAuth
                        ?: (emailFromAuth?.substringBefore('@') ?: ""),
                    email = doc.getString("email") ?: emailFromAuth ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatar = doc.getString("avatarUrl") ?: avatarFromAuth ?: "",
                    address = doc.getString("address") ?: "",
                    totalBookings = (doc.getLong("totalBookings") ?: 0L).toInt(),
                    totalReviews = (doc.getLong("totalReviews") ?: 0L).toInt(),
                    averageRating = (doc.getDouble("averageRating") ?: 0.0).toFloat()
                )
                onSuccess(user)
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
        if (avatarUrl != null) updates["avatarUrl"] = avatarUrl

        firestore.collection("users").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener { getCurrentUserProfile(onSuccess, onError) }
            .addOnFailureListener { e -> onError(e) }
    }
}


