package com.trungkien.fbtp_cn

import android.app.Application
import com.google.firebase.FirebaseApp

class FBTPApp : Application() {// dùng để khởi tạo FirebaseApp khi ứng dụng được tạo và quản lý các thành phần toàn cục khác nếu cần
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}