package com.trungkien.fbtp_cn.ui.components.owner.map

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Custom marker icon cho từng loại sân thể thao
 * Dựa trên hình ảnh mẫu với các icon khác nhau cho từng loại sân
 */
class SportMarkerIcon(
    private val context: Context,
    private val sportType: String,
    private val size: Int = 80
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        // Set bounds để đảm bảo marker có kích thước đúng
        setBounds(0, 0, size, size)
        
        // Màu nền cho marker theo loại sân
        val backgroundColor = getSportColor(sportType)
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        
        // Stroke màu trắng cho border
        strokePaint.color = Color.WHITE
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 4f
        
        // Màu cho icon
        iconPaint.color = Color.WHITE
        iconPaint.style = Paint.Style.FILL
        iconPaint.textAlign = Paint.Align.CENTER
        iconPaint.textSize = 24f
        iconPaint.typeface = Typeface.DEFAULT_BOLD
        
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        
        if (width <= 0 || height <= 0) {
            return
        }
        
        // Tính toán kích thước thực tế - sử dụng toàn bộ không gian để marker rõ ràng hơn
        val markerWidth = width
        val markerHeight = height
        
        // Vẽ marker shape (hình giọt nước - teardrop shape)
        drawTeardropShape(canvas, 0, 0, markerWidth, markerHeight)
        
        // Vẽ icon của loại sân ở giữa marker
        drawSportIcon(canvas, 0, 0, markerWidth, markerHeight)
        
    }

    private fun drawTeardropShape(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val radius = (right - left) / 2f
        
        // Tạo path cho hình giọt nước (teardrop)
        val teardropPath = Path()
        
        // Phần trên tròn - đặt ở giữa để marker cân đối
        val circleTop = centerY - radius * 0.1f
        teardropPath.addCircle(centerX, circleTop, radius * 0.8f, Path.Direction.CW)
        
        // Phần đuôi nhọn - ngắn hơn để marker không quá dài
        val tailHeight = radius * 0.3f
        val tailWidth = radius * 0.2f
        
        teardropPath.moveTo(centerX, bottom.toFloat())
        teardropPath.lineTo(centerX - tailWidth, centerY + radius * 0.4f)
        teardropPath.lineTo(centerX + tailWidth, centerY + radius * 0.4f)
        teardropPath.close()
        
        // Vẽ hình giọt nước với màu nền
        canvas.drawPath(teardropPath, paint)
        
        // Vẽ border trắng
        canvas.drawPath(teardropPath, strokePaint)
    }
    
    private fun drawMarkerShape(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val radius = (right - left) / 2f
        
        // Vẽ hình tròn chính
        canvas.drawCircle(centerX, centerY, radius, paint)
        canvas.drawCircle(centerX, centerY, radius, strokePaint)
        
        // Vẽ đuôi nhọn ở dưới
        val tailLength = radius * 0.3f
        val tailWidth = radius * 0.2f
        
        path.reset()
        path.moveTo(centerX, bottom.toFloat())
        path.lineTo(centerX - tailWidth, centerY + radius + tailLength)
        path.lineTo(centerX + tailWidth, centerY + radius + tailLength)
        path.close()
        
        canvas.drawPath(path, paint)
        canvas.drawPath(path, strokePaint)
    }

    private fun drawSportIcon(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f - (right - left) / 6f // Dịch lên ít hơn để cân đối

        // Nền hình tròn trắng ở giữa để emoji nổi bật
        val innerRadius = (right - left) * 0.25f
        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, innerRadius, innerCirclePaint)

        // Vẽ emoji theo loại sân
        val emoji = when (sportType.uppercase()) {
            "FOOTBALL" -> "⚽"
            "BADMINTON" -> "🏸"
            "TENNIS" -> "🎾"
            "PICKLEBALL" -> "🏓"
            else -> "❖"
        }

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            // Emoji kích thước lớn hơn một chút để dễ nhìn trong preview và trên map
            textSize = (right - left) * 0.4f
        }
        // Điều chỉnh baseline để emoji thật sự ở giữa (approximation)
        val textBounds = Rect()
        emojiPaint.getTextBounds(emoji, 0, emoji.length, textBounds)
        val textHeight = textBounds.height()
        canvas.drawText(emoji, centerX, centerY + textHeight / 2f - 2f, emojiPaint)
    }

    private fun drawTennisIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Vẽ vợt tennis
        val racketSize = 20f
        val handleLength = 15f
        
        // Vòng vợt
        canvas.drawCircle(centerX, centerY - 5f, racketSize, iconPaint)
        
        // Cán vợt
        canvas.drawRect(
            centerX - 2f, centerY + 5f,
            centerX + 2f, centerY + 5f + handleLength,
            iconPaint
        )
        
        // Quả bóng tennis
        canvas.drawCircle(centerX + 15f, centerY - 10f, 5f, iconPaint)
    }

    private fun drawBadmintonIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Vẽ vợt cầu lông
        val racketSize = 18f
        val handleLength = 12f
        
        // Vòng vợt
        canvas.drawCircle(centerX, centerY - 3f, racketSize, iconPaint)
        
        // Cán vợt
        canvas.drawRect(
            centerX - 1.5f, centerY + 3f,
            centerX + 1.5f, centerY + 3f + handleLength,
            iconPaint
        )
        
        // Quả cầu lông
        canvas.drawCircle(centerX + 12f, centerY - 8f, 3f, iconPaint)
    }

    private fun drawFootballIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Vẽ quả bóng đá
        val ballSize = 16f
        
        // Hình lục giác đơn giản cho quả bóng
        val hexagonPath = Path()
        val radius = ballSize
        
        for (i in 0..5) {
            val angle = Math.PI / 3 * i
            val x = centerX + radius * Math.cos(angle).toFloat()
            val y = centerY + radius * Math.sin(angle).toFloat()
            
            if (i == 0) {
                hexagonPath.moveTo(x, y)
            } else {
                hexagonPath.lineTo(x, y)
            }
        }
        hexagonPath.close()
        
        canvas.drawPath(hexagonPath, iconPaint)
    }

    private fun drawPickleballIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Vẽ vợt pickleball (giống tennis nhưng nhỏ hơn)
        val racketSize = 16f
        val handleLength = 10f
        
        // Vòng vợt
        canvas.drawCircle(centerX, centerY - 3f, racketSize, iconPaint)
        
        // Cán vợt
        canvas.drawRect(
            centerX - 1.5f, centerY + 3f,
            centerX + 1.5f, centerY + 3f + handleLength,
            iconPaint
        )
        
        // Quả bóng pickleball
        canvas.drawCircle(centerX + 12f, centerY - 6f, 4f, iconPaint)
    }


    private fun drawDefaultIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, 12f, innerCirclePaint)
        val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 18f
        }
        canvas.drawText("❖", centerX, centerY + 6f, fallbackPaint)
    }

    private fun getSportColor(sportType: String): Int {
        return when (sportType.uppercase()) {
            "TENNIS" -> Color.parseColor("#FF4444") // Đỏ
            "BADMINTON" -> Color.parseColor("#4CAF50") // Xanh lá
            "FOOTBALL" -> Color.parseColor("#2196F3") // Xanh dương
            "PICKLEBALL" -> Color.parseColor("#9C27B0") // Tím
            else -> Color.parseColor("#607D8B") // Xám xanh (mặc định)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        strokePaint.alpha = alpha
        iconPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
        iconPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int = size

    override fun getIntrinsicHeight(): Int = size
}

// -------------------- Previews --------------------
@Composable
private fun MarkerPreview(sport: String) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.size(120.dp),
        factory = { ctx ->
            ImageView(ctx).apply {
                val d = SportMarkerIcon(context, sport, 120)
                setImageDrawable(d)
            }
        }
    )
}

@Preview(name = "Tennis Marker")
@Composable
fun Preview_TennisMarker() {
    MarkerPreview("TENNIS")
}

@Preview(name = "Badminton Marker")
@Composable
fun Preview_BadmintonMarker() {
    MarkerPreview("BADMINTON")
}

@Preview(name = "Football Marker")
@Composable
fun Preview_FootballMarker() {
    MarkerPreview("FOOTBALL")
}

@Preview(name = "Pickleball Marker")
@Composable
fun Preview_PickleballMarker() {
    MarkerPreview("PICKLEBALL")
}
