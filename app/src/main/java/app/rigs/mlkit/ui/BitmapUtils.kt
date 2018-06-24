package app.rigs.mlkit.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20

object BitmapUtils {

    private val textureMaxDimension: Int

    init {
        // we need an OpenGL context to get the limits of the texture size for this device
        textureMaxDimension = updateTextureMaxDimension()
    }

    private fun updateTextureMaxDimension(): Int {
        val display = getEGLDisplay()
        val config = getEGLConfig(display)
        val surface = getEGLSurface(display, config)
        val eglContext = getEGLContext(display, config)
        val maxSize = getMaximumTextureSize(display, surface, eglContext)
        cleanUpEGLContext(display, surface, eglContext)
        return maxSize
    }

    private fun getEGLDisplay(): EGLDisplay {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(display, version, 0, version, 1)
        return display
    }

    private fun getEGLConfig(display: EGLDisplay): EGLConfig {
        // now we need a render surface
        val configAttr = intArrayOf(
            EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
            EGL14.EGL_LEVEL, 0,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfig = IntArray(1)
        EGL14.eglChooseConfig(display, configAttr, 0, configs, 0, 1, numConfig, 0)

        // If this fails, there's so much wrong with this device, it's not even funny
        return configs[0]!!
    }

    private fun getEGLSurface(display: EGLDisplay, config: EGLConfig): EGLSurface {
        val surfAttr = intArrayOf(
            EGL14.EGL_WIDTH, 64,
            EGL14.EGL_HEIGHT, 64,
            EGL14.EGL_NONE
        )

        return EGL14.eglCreatePbufferSurface(display, config, surfAttr, 0)
    }

    private fun getEGLContext(display: EGLDisplay, config: EGLConfig): EGLContext {
        val ctxAttr = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        return EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, ctxAttr, 0)
    }

    private fun getMaximumTextureSize(display: EGLDisplay, surface: EGLSurface, eglContext: EGLContext): Int {
        EGL14.eglMakeCurrent(display, surface, surface, eglContext)
        val maxSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        return maxSize[0]
    }

    private fun cleanUpEGLContext(display: EGLDisplay, surface: EGLSurface, eglContext: EGLContext) {
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(display, surface)
        EGL14.eglDestroyContext(display, eglContext)
        EGL14.eglTerminate(display)
    }

    fun loadBitmap(
        context: Context,
        imageUri: Uri? = null,
        resourceId: Int? = null,
        requiredWidth: Int = 0,
        requiredHeight: Int = 0
    ): Bitmap {
        val decodeOptions = getDecodeOnlyOptions()

        when {
            imageUri != null -> BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri),
                null,
                decodeOptions
            )
            resourceId != null -> BitmapFactory.decodeResource(context.resources, resourceId, decodeOptions)
            else -> throw Exception("Must specify a bitmap resource to load.")
        }

        updateDecodeOptionsForLoading(decodeOptions, requiredWidth, requiredHeight)

        var bitmap = Bitmap.createScaledBitmap(
            when {
                imageUri != null -> BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(imageUri),
                    null,
                    decodeOptions
                )
                resourceId != null -> BitmapFactory.decodeResource(context.resources, resourceId, decodeOptions)
                else -> throw Exception("Unexpected exception in loadBitmapImage.")
            },
            normaliseDimension(decodeOptions.outWidth),
            normaliseDimension(decodeOptions.outHeight),
            false
        )
        if (imageUri != null) {
            bitmap = ExifUtil.rotateBitmap(
                bitmap,
                ExifUtil.getOrientation(context.contentResolver.openInputStream(imageUri))
            )
        }
        return bitmap
    }

    private fun getDecodeOnlyOptions(): BitmapFactory.Options {
        val decodeOptions: BitmapFactory.Options = BitmapFactory.Options()
        decodeOptions.inJustDecodeBounds = true
        return decodeOptions
    }

    private fun normaliseDimension(dimension: Int): Int {
        return Math.min(dimension, textureMaxDimension)
    }
    /**
     * Basically stole this from Google
     * https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
     *
     * Figure out what's the best sampling size to get us as close as possible to the required size of the bitmap image.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun updateDecodeOptionsForLoading(
        decodeOptions: BitmapFactory.Options,
        requiredWidth: Int,
        requiredHeight: Int
    ) {
        decodeOptions.inSampleSize = calculateInSampleSize(
            decodeOptions,
            normaliseDimension(if (requiredWidth == 0) decodeOptions.outWidth else requiredWidth),
            normaliseDimension(if (requiredHeight == 0) decodeOptions.outHeight else requiredHeight)
        )
        decodeOptions.inScaled = true

        decodeOptions.inJustDecodeBounds = false
    }
}