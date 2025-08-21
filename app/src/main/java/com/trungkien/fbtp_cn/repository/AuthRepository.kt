package com.trungkien.fbtp_cn.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles authentication and user profile persistence in Firestore.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Registers a new account with email/password and persists a user document in Firestore.
     * The [role] can be provided as Vietnamese labels ("Chủ sân", "Khách hàng") or canonical
     * values ("OWNER", "RENTER"). It will be normalized before persistence.
     */
    fun registerUser(
        username: String,
        password: String,
        email: String,
        phone: String,
        role: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // Create account in Firebase Auth directly (avoid Firestore read which may be blocked by rules)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError(IllegalStateException("User ID is null after registration"))
                    return@addOnSuccessListener
                }

                val normalizedRole = when {
                    role.equals("Chủ sân", ignoreCase = true) -> "OWNER"
                    role.equals("Khách hàng", ignoreCase = true) -> "RENTER"
                    role.equals("OWNER", ignoreCase = true) -> "OWNER"
                    role.equals("RENTER", ignoreCase = true) -> "RENTER"
                    else -> "RENTER"
                }

                val userDoc = hashMapOf(
                    "userId" to uid,
                    "role" to normalizedRole,
                    "name" to username,
                    "email" to email,
                    "phone" to phone,
                    "avatarUrl" to "",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users").document(uid)
                    .set(userDoc)
                    .addOnSuccessListener { onSuccess(uid) }
                    .addOnFailureListener { e -> onError(e) }
            }
            .addOnFailureListener { e ->
                // Map duplicate email to friendly message
                if (e is FirebaseAuthUserCollisionException || (e.message ?: "").contains("already in use", ignoreCase = true)) {
                    onError(IllegalStateException("Email đã được sử dụng"))
                } else {
                    onError(e)
                }
            }
    }

    /**
     * Logs in with email/password then fetches user role from Firestore users/{uid}.
     */
    fun login(
        email: String,
        password: String,
        onSuccess: (role: String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener onError(IllegalStateException("UID null"))
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val role = (doc.getString("role") ?: "RENTER").uppercase()
                        onSuccess(role)
                    }
                    .addOnFailureListener { e -> onError(e) }
            }
            .addOnFailureListener { e -> onError(e) }
    }
}


