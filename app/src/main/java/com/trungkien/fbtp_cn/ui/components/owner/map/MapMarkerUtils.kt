package com.trungkien.fbtp_cn.ui.components.owner.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.trungkien.fbtp_cn.R

/**
 * Utility functions để đảm bảo marker hiển thị đúng vị trí trên Google Maps
 */
object MapMarkerUtils {

    /**
     * Validate tọa độ có hợp lệ không
     */
    fun isValidLocation(location: GeoLocation): Boolean {
        return location.lat != 0.0 && location.lng != 0.0 &&
               location.lat >= -90.0 && location.lat <= 90.0 &&
               location.lng >= -180.0 && location.lng <= 180.0
    }

    /**
     * Tạo LatLng từ GeoLocation với validation
     */
    fun createLatLng(location: GeoLocation): LatLng? {
        return if (isValidLocation(location)) {
            LatLng(location.lat, location.lng)
        } else {
            null
        }
    }

    /**
     * Chuyển đổi SportMarkerIcon thành BitmapDescriptor cho Google Maps
     */
    fun getSportMarkerBitmapDescriptor(context: Context, sportType: String, size: Int): BitmapDescriptor {
        // Sử dụng SportMarkerIcon custom class thay vì drawable resources
        val sportMarkerIcon = SportMarkerIcon(context, sportType, size)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        sportMarkerIcon.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}