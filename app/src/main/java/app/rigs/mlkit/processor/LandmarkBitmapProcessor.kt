package app.rigs.mlkit.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark

class LandmarkBitmapProcessor {

    fun drawBoundingBoxes(bitmap: Bitmap, landmarks: List<FirebaseVisionCloudLandmark>): Bitmap? {
        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)


        for (landmark in landmarks) {
            canvas.withSave {
                drawRoundRect(RectF(landmark.boundingBox), 0f, 0f, rectPaint )
            }
        }
        return temporaryBitmap
    }

    private val rectPaint =
        Paint().apply {
            strokeWidth = 5f
            color = Color.CYAN
            style = Paint.Style.STROKE
        }
}