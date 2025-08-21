package com.trungkien.fbtp_cn

import android.app.Application
import com.google.firebase.FirebaseApp

class FBTPApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}