package io.github.chhabra_dhiraj.webcamdc

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

//    private lateinit var cameraExecutor: ExecutorService
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize camera and executor
//        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        // Establish a socket connection to the macOS server
//        startSocketConnection()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, imageCapture
            )

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
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//        socket?.close()
//    }
}