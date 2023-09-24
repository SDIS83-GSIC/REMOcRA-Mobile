package fr.sdis83.remocra.mobile.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction1

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
    onUseCase: (UseCase) -> Unit = { },
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            onUseCase(
                Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                        targetRotation = context.display!!.rotation
                    },
            )
            previewView
        },
    )
}

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onPictureTaken: KFunction1<Bitmap, Unit>,
    onClose: () -> Unit = { },
) {
    Box(modifier = modifier) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()
        var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
        val imageCaptureUseCase by remember {
            mutableStateOf(
                ImageCapture.Builder()
                    .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build(),
            )
        }
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onUseCase = {
                previewUseCase = it
            },
        )
        LaunchedEffect(previewUseCase) {
            val cameraProvider = context.getCameraProvider()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageCaptureUseCase,
                )
            } catch (ex: Exception) {
                Log.e("CameraCapture", "Failed to bind camera use cases", ex)
            }
        }
        Row(
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.BottomCenter),
        ) {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                    coroutineScope.launch {
                        imageCaptureUseCase.takePicture(
                            context.executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                @SuppressLint("UnsafeOptInUsageError")
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    onPictureTaken.invoke(
                                        imageProxy.image!!.toBitmap(),
                                    )
                                    imageProxy.close()
                                }

                                override fun onError(exception: ImageCaptureException) {
                                }
                            },
                        )
                        onClose()
                    }
                },
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Photo")
            }
        }
    }
}

@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE")
fun Image.toBitmap(): Bitmap = toImage()

private fun Image.toImage(): Bitmap {
    val buffer = planes[0].buffer

    val size = buffer.remaining()

    val array = ByteArray(size)

    buffer.get(array, 0, size)

    return BitmapFactory.decodeByteArray(array, 0, array.size)
}

private fun Image.toYuvImage(): YuvImage {
    val yBuffer = planes[0].buffer // Y

    val uBuffer = planes[1].buffer // U

    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    return YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
}

suspend fun ImageCapture.takePicture(executor: Executor, outputFile: File) {
//    val photoFile = withContext(Dispatchers.IO) {
//        kotlin.runCatching {
//            File.createTempFile("image", ".jpg")
//        }.getOrElse { ex ->
//            Log.e("TakePicture", "Failed to create temporary file", ex)
//            File("/dev/null")
//        }
//    }

    return suspendCoroutine { continuation ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.i("TakePicture", outputFile.absolutePath)
                }

                override fun onError(ex: ImageCaptureException) {
                    Log.e("TakePicture", "Image capture failed", ex)
                    continuation.resumeWithException(ex)
                }
            },
        )
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)
