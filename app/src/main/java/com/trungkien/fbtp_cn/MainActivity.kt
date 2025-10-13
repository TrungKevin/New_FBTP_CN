package com.trungkien.fbtp_cn

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                                onLogoutToSplash = { currentScreen = "splash" }
                            )
                        }
                        "renter" -> {
                            RenterMainScreen(
                                modifier = Modifier.fillMaxSize(),
                                onLogoutToSplash = { currentScreen = "splash" }
                            )
                        }
                    }
                }
            }
        }
    }
}

