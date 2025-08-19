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
import androidx.compose.ui.Modifier
import com.trungkien.fbtp_cn.ui.screens.owner.OwnerMainScreen
import com.trungkien.fbtp_cn.ui.screens.renter.RenterMainScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FBTP_CNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Test RenterMainScreen thay vì OwnerMainScreen
                    RenterMainScreen(
                        modifier = Modifier.fillMaxSize()
                    )
//                    OwnerMainScreen(
//                        modifier = Modifier.fillMaxSize()
//                    )
                }
            }
        }
    }
}

