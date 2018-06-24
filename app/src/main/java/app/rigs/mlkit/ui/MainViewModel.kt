package app.rigs.mlkit.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import app.rigs.mlkit.processor.BarcodeBitmapProcessor
import app.rigs.mlkit.processor.LandmarkBitmapProcessor
import app.rigs.mlkit.processor.SnapchatifyBitmapProcessor
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions



class MainViewModel(application: Application) : AndroidViewModel(application) {

    val snapchatifyBitmapProcessor  = SnapchatifyBitmapProcessor(application)


    val barcodeBitmapProcessor = BarcodeBitmapProcessor()


    var processedBitmap : MutableLiveData<Bitmap> = MutableLiveData()

    val textResult = MutableLiveData<String>()

    fun faceDetect(bitmap: Bitmap?) {
        bitmap?.let {
            doFaceAnnotation(bitmap)
        }
    }

    fun barcodeDetect(bitmap: Bitmap?){
        bitmap?.let {
            doBarcodeDetection(bitmap)
        }
    }

    fun landmarkDetect(bitmap: Bitmap?){
        bitmap?.let {
            doLandmarkDetection(bitmap)
        }
    }

    private fun doBarcodeDetection(bitmap: Bitmap) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        detector.detectInImage(image)
            .addOnSuccessListener {
                processedBitmap.postValue( barcodeBitmapProcessor.drawBoundingBoxes(bitmap, it))
                var result = String()
                it.forEach {
                    result += " VALUE TYPE: ${it.valueType} FORMAT: ${it.format} Raw Value: ${it.rawValue}"
                    textResult.postValue(result)
                    Log.d("Barcode logs:", result)
                }
            }
            .addOnFailureListener{
                textResult.postValue(it.message)
                Log.e("Barcode logs:", " Failed to find barcode", it)
            }
    }

    private fun doFaceAnnotation(bitmap: Bitmap) {

        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .build()

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        detector
            .detectInImage(image)
            .addOnSuccessListener {
               processedBitmap.postValue( snapchatifyBitmapProcessor.annotateFace(bitmap, it))
            }
            .addOnFailureListener {
                Toast.makeText(getApplication(), "Error detecting faces $it", Toast.LENGTH_SHORT).show()
            }
    }

    private val landmarkBitmapProcessor = LandmarkBitmapProcessor()

    private fun doLandmarkDetection(bitmap: Bitmap){

        val options = FirebaseVisionCloudDetectorOptions.Builder()
            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
            .setMaxResults(5)
            .build()

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance()
            .getVisionCloudLandmarkDetector(options)

        detector.detectInImage(image)
            .addOnSuccessListener {
                processedBitmap.postValue( landmarkBitmapProcessor.drawBoundingBoxes(bitmap, it))
                var result = String()
                if (it.size == 0){
                    Toast.makeText(getApplication(), "No Landmarks detected", Toast.LENGTH_LONG).show()
                }
                it.forEach {
                    result += " LANDMARK: ${it.landmark} . Confidence: ${it.confidence}"
                    textResult.postValue(result)
                    Log.d("Barcode logs:", result)
                }
            }
            .addOnFailureListener{
                Toast.makeText(getApplication(), "Error detecting landmarks $it", Toast.LENGTH_SHORT).show()
            }
    }

}
