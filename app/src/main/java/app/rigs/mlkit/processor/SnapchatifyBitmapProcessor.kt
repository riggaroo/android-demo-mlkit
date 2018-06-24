package app.rigs.mlkit.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import app.rigs.mlkit.R
import app.rigs.mlkit.ui.BitmapUtils
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark

class SnapchatifyBitmapProcessor(val context: Context) {

    private val mouthBitmap: Bitmap = BitmapUtils.loadBitmap(context, null, R.drawable.mouth)
    private val eyeBitmap: Bitmap = BitmapUtils.loadBitmap(context, null, R.drawable.heart)

    private val mouthPaint =
        Paint().apply {
            reset()
            isAntiAlias = true
            isFilterBitmap = true
        }


    private val rectPaint =
        Paint().apply {
            strokeWidth = 5f
            color = Color.CYAN
            style = Paint.Style.STROKE
        }

    fun annotateFace(bitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)


        for (face in faces) {
            val bounds = face.boundingBox

            val rotY = face.headEulerAngleY  // Head is rotated to the right rotY degrees
            val rotZ = face.headEulerAngleZ  // Head is tilted sideways rotZ degrees

            //mouth
            drawMouth(canvas, face, rotY)

            val eyeSize = (bounds.width()) * 0.15f
            val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
            if (rightEye != null){
                drawEye(canvas, rightEye, eyeSize)
            }

            val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
            if (leftEye != null){
                drawEye(canvas, leftEye, eyeSize)
            }

            /* // To draw a bounding box over the Face uncomment below
            canvas.withSave {
                    drawRoundRect(RectF(bounds), rotY, rotZ, rectPaint)
            }*/
        }
        return temporaryBitmap
    }

    private fun drawEye(canvas: Canvas, eye: FirebaseVisionFaceLandmark, eyeSize: Float){
        eye.position?.let {
            canvas.withSave {
                withTranslation(it.x, it.y) {
                    eyeBitmap.let {
                        drawBitmap(it, null, RectF(-eyeSize, -eyeSize,
                            eyeSize, eyeSize), mouthPaint)
                    }
                }
            }
        }
    }

    private fun drawMouth(canvas: Canvas, face: FirebaseVisionFace, rotY: Float) {
        val leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_MOUTH)
        leftMouth?.position?.let {

            canvas.drawCircle(it.x, it.y, 4f, rectPaint)
        }

        val rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_MOUTH)
        rightMouth?.position?.let {

            canvas.drawCircle(it.x, it.y, 4f, rectPaint)
        }

        val bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.BOTTOM_MOUTH)
        bottomMouth?.position?.let {

            canvas.drawCircle(it.x, it.y, 4f, rectPaint)
        }
        val bottomNose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)

        bottomNose?.position?.let {

            canvas.drawCircle(it.x, it.y, 4f, rectPaint)
        }


        if (leftMouth != null && rightMouth != null && bottomMouth != null && bottomNose != null) {
            val mouthSizeWidth = leftMouth.position.x - rightMouth.position.x
            val mouthSizeHeight = bottomMouth.position.y - bottomNose.position.y
            val centreX = mouthSizeWidth / 2 + rightMouth.position.x
            val centreY = mouthSizeHeight / 2 + bottomNose.position.y - 10
            canvas.withSave {
                withRotation(rotY, leftMouth.position.x, leftMouth.position.y) {
                    withTranslation(centreX, centreY) {
                        mouthBitmap.let {
                            drawBitmap(it, null, RectF(-mouthSizeWidth / 2f * 1.5f, -mouthSizeHeight / 2f * 1.5f,
                                mouthSizeWidth / 2f * 1.5f, mouthSizeHeight / 2f * 1.5f), mouthPaint)
                        }
                    }
                }
            }
        }
    }
}