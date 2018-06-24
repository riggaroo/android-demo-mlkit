package app.rigs.mlkit.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import android.support.media.ExifInterface
import java.io.IOException
import java.io.InputStream

object ExifUtil {
    /**
     * Rotates the bitmap according to the orientation which can be obtained from the Exif Data from the image.
     */
    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return bmRotated
    }

    /**
     * Uses the Exif data on an image to get the correct orientation of the image.
     */
    fun getOrientation(inputStream: InputStream): Int {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(inputStream)
        } catch (e: IOException) {
            //Timber.e(e, "Exception getting ExifInterface information")
        }

        return exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED) ?: ExifInterface.ORIENTATION_NORMAL
    }
}