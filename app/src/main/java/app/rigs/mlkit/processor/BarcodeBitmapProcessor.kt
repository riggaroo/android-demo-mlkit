package app.rigs.mlkit.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

class BarcodeBitmapProcessor {

    fun drawBoundingBoxes(bitmap: Bitmap, firebaseVisionBarcode: List<FirebaseVisionBarcode>) : Bitmap {
        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)


        for (barcode in firebaseVisionBarcode) {
            canvas.withSave {
                drawRoundRect(RectF(barcode.boundingBox), 0f, 0f, rectPaint )
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