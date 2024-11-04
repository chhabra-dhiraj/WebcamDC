package io.github.chhabra_dhiraj.webcamdc

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    private var socket: Socket? = null

    private var mediaCodec: MediaCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = findViewById<PreviewView>(R.id.viewFinder).surfaceProvider
            }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        Log.e(TAG, "Dhiraj socket 1 connected")

        imageAnalysis.setAnalyzer(cameraExecutor) { image ->
            try {
                Log.e(TAG, "Dhiraj socket 2 connected")
                encodeAndSendFrame(image)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error encoding or sending frame", e)
            } finally {
                image.close()
            }
        }

        cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
    }

    private fun encodeAndSendFrame(image: ImageProxy) {
        Log.e(TAG, "Dhiraj socket 3 connected")
//        // 1. Initialize MediaCodec if not already initialized
//        if (mediaCodec == null) {
//            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
//            val format = MediaFormat.createVideoFormat(
//                MediaFormat.MIMETYPE_VIDEO_AVC,
//                image.width,
//                image.height
//            )
//            // Configure MediaFormat with desired encoding parameters
//            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//            mediaCodec?.start()
//        }
//
//        // 2. Get image data
//        val buffer = image.planes[0].buffer
//        val bufferSize = buffer.remaining()
//        val data = ByteArray(bufferSize)
//        buffer.get(data)
//
//        // 3. Encode frame
//        val inputBufferIndex = mediaCodec?.dequeueInputBuffer(TIMEOUT_US) ?: 0
//        Log.e(TAG, "Dhiraj socket 4 " +
//                "connected inputBufferIndex: $inputBufferIndex")
//        if (inputBufferIndex >= 0) {
//            val inputBuffer = mediaCodec?.getInputBuffer(inputBufferIndex)
//            inputBuffer?.put(data)
//            mediaCodec?.queueInputBuffer(
//                inputBufferIndex,
//                0,
//                bufferSize,
//                image.imageInfo.timestamp,
//                0
//            )
//        }
//
//        // 4. Send encoded data over socket
//        val bufferInfo = MediaCodec.BufferInfo()
//        val outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, TIMEOUT_US) ?: 0
//        Log.e(TAG, "Dhiraj socket 5 " +
//                "connected outputBufferIndex: $outputBufferIndex")
//        if (outputBufferIndex >= 0) {
//            val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferIndex)
//            val encodedData = ByteArray(bufferInfo.size)
//            outputBuffer?.get(encodedData)

            // Send encodedData over socket
            if (socket == null || socket?.isClosed == true) {
                socket = Socket("192.168.0.107", 8080)
            }
                Log.e(TAG, "Dhiraj socket 6 " +
                        "connected")
            val lSocket = socket ?: return

            lSocket.outputStream.write(24)
//            lSocket.outputStream.write(encodedData)

//            mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
//        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
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
        mediaCodec?.stop()
        mediaCodec?.release()
        socket?.close()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "WebcamDCVideoStreaming"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        private const val TIMEOUT_US = 10000L
    }
}