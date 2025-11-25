package com.trungkien.fbtp_cn

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.trungkien.fbtp_cn.ui.screens.MainSplashScreen
import com.trungkien.fbtp_cn.ui.screens.owner.OwnerMainScreen
import com.trungkien.fbtp_cn.ui.screens.renter.RenterMainScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // ✅ Lấy FCM Token để test thông báo
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("❌ ERROR: Failed to get FCM token: ${task.exception}")
                return@addOnCompleteListener
            }
            
            val token = task.result
            println("🔔 FCM Token: $token")
            println("🔔 Copy token này để test thông báo từ Firebase Console")
        }
        
        setContent {
            FBTP_CNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    val logoutAndNavigate: () -> Unit = {
                        performFullLogout()
                        currentScreen = "splash"
                    }

                    when (currentScreen) {
                        "splash" -> {
                            MainSplashScreen(
                                onNavigateToOwner = {
                                    currentScreen = "owner"
                                },
                                onNavigateToRenter = {
                                    currentScreen = "renter"
                                }
                            )
                        }
                        "owner" -> {
                            OwnerMainScreen(
                                modifier = Modifier.fillMaxSize(),
                                onLogoutToSplash = logoutAndNavigate
                            )
                        }
                        "renter" -> {
                            RenterMainScreen(
                                modifier = Modifier.fillMaxSize(),
                                onLogoutToSplash = logoutAndNavigate
                            )
                        }
                    }
                }
            }
        }
    }

    private fun performFullLogout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w("Logout", "Firebase signOut failed", e)
        }
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.trungkien.fbtp_cn.R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            googleClient.signOut()
            googleClient.revokeAccess()
        } catch (e: Exception) {
            Log.w("Logout", "Google signOut failed", e)
        }
        try {
            viewModelStore.clear()
        } catch (e: Exception) {
            Log.w("Logout", "ViewModelStore clear failed", e)
        }
    }
}

