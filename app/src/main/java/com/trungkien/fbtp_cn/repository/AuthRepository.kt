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
        // Pre-validate inputs to avoid unnecessary network calls
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            onError(IllegalArgumentException("Thông tin không được để trống"))
            return
        }
        
        if (password.length < 6) {
            onError(IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự"))
            return
        }

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
                // Map duplicate email to friendly message with better error handling
                when {
                    e is FirebaseAuthUserCollisionException -> {
                        onError(IllegalStateException("Email đã được sử dụng"))
                    }
                    (e.message ?: "").contains("already in use", ignoreCase = true) -> {
                        onError(IllegalStateException("Email đã được sử dụng"))
                    }
                    (e.message ?: "").contains("network", ignoreCase = true) -> {
                        onError(IllegalStateException("Lỗi kết nối mạng. Vui lòng thử lại"))
                    }
                    (e.message ?: "").contains("timeout", ignoreCase = true) -> {
                        onError(IllegalStateException("Kết nối quá chậm. Vui lòng thử lại"))
                    }
                    else -> {
                        onError(e)
                    }
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

    /**
     * Sends password reset email to the given address.
     */
    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    /**
     * Deletes current Firebase Auth user and its user document in Firestore.
     * Note: Firebase may require recent login to delete; caller should handle error message.
     */
    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        val uid = user?.uid
        if (user == null || uid.isNullOrBlank()) {
            onError(IllegalStateException("Chưa đăng nhập"))
            return
        }
        // First delete Firestore profile, then Auth user
        firestore.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e) }
            }
            .addOnFailureListener { e ->
                // Even if Firestore delete fails, attempt to delete auth user to avoid being stuck
                user.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { ee -> onError(ee) }
            }
    }
}


