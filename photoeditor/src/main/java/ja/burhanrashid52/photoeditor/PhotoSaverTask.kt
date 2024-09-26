package ja.burhanrashid52.photoeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ja.burhanrashid52.photoeditor.BitmapUtil.removeTransparency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Burhanuddin Rashid on 18/05/21.
 *
 * @author <https:></https:>//github.com/burhanrashid52>
 */
internal class PhotoSaverTask(
    private val photoEditorView: PhotoEditorView,
    private val boxHelper: BoxHelper,
    private var saveSettings: SaveSettings
) {

    private val drawingView: DrawingView = photoEditorView.drawingView

    private fun onBeforeSaveImage() {
        boxHelper.clearHelperBox()
        drawingView.destroyDrawingCache()
    }

    fun saveImageAsBitmap(): Bitmap {
        onBeforeSaveImage()
        val bitmap = buildBitmap()
        if (saveSettings.isClearViewsEnabled) {
            boxHelper.clearAllViews(drawingView)
        }
        return bitmap
    }

    suspend fun saveImageAsFile(imagePath: String): SaveFileResult {
        onBeforeSaveImage()
        val capturedBitmap = buildBitmap()

        val result = withContext(Dispatchers.IO) {
            val file = File(imagePath)
            try {
                FileOutputStream(file, false).use { outputStream ->
                    capturedBitmap.compress(
                        saveSettings.compressFormat,
                        saveSettings.compressQuality,
                        outputStream
                    )
                    outputStream.flush()
                }

                SaveFileResult.Success
            } catch (e: IOException) {
                SaveFileResult.Failure(e)
            }
        }

        if (result is SaveFileResult.Success) {
            // Clear all views if it's enabled in save settings
            if (saveSettings.isClearViewsEnabled) {
                boxHelper.clearAllViews(drawingView)
            }
        }

        return result
    }

    private fun buildBitmap(): Bitmap {
        return if (saveSettings.isTransparencyEnabled) {
            removeTransparency(captureView(photoEditorView))
        } else {
            captureView(photoEditorView)
        }
    }

    private fun captureView(view: View): Bitmap {
        convertHardwareBitmapsToSoftware(view)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun convertHardwareBitmapsToSoftware(view: View) {
        if (view is ImageView) {
            val drawable = view.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                if (bitmap.config == Bitmap.Config.HARDWARE) {
                    // 하드웨어 비트맵을 소프트웨어 비트맵으로 변환
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    view.setImageBitmap(softwareBitmap)
                }
            }
        }

        // 자식 뷰가 있는 경우에도 하드웨어 비트맵 변환 적용
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                convertHardwareBitmapsToSoftware(view.getChildAt(i))
            }
        }
    }
}