package io.github.chhabra_dhiraj.webcamdc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Initialize camera and executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Establish a socket connection to the macOS server
//        startSocketConnection()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = findViewById<PreviewView>(R.id.viewFinder).surfaceProvider
                }
            val imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

            // Start capturing images periodically and sending to the server
//            startStreaming(imageCapture)

        }, ContextCompat.getMainExecutor(this))
    }

//    private fun startStreaming(imageCapture: ImageCapture) {
//        val intervalMillis = 100 // Adjust for the desired frame rate
//        val runnable = object : Runnable {
//            override fun run() {
//                imageCapture.takePicture(ContextCompat.getMainExecutor(this@MainActivity), object : ImageCapture.OnImageCapturedCallback() {
//                    override fun onCaptureSuccess(image: ImageProxy) {
//                        val byteArray = imageToByteArray(image)
//                        sendFrame(byteArray)
//                        image.close()
//                    }
//                })
//                cameraExecutor.schedule(this, intervalMillis.toLong(), TimeUnit.MILLISECONDS)
//            }
//        }
//        cameraExecutor.schedule(runnable, intervalMillis.toLong(), TimeUnit.MILLISECONDS)
//    }
//
//    private fun imageToByteArray(image: ImageProxy): ByteArray {
//        val bitmap = // Convert ImageProxy to Bitmap
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream) // Compress to reduce size
//        return stream.toByteArray()
//    }
//
//    private fun startSocketConnection() {
//        Thread {
//            try {
//                socket = Socket("localhost", 9000) // Mac server IP
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }.start()
//    }
//
//    private fun sendFrame(frame: ByteArray) {
//        socket?.getOutputStream()?.write(frame)
//        socket?.getOutputStream()?.flush()
//    }
//

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
//        socket?.close()
    }

    companion object {
        private const val TAG = "WebcamDC"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}