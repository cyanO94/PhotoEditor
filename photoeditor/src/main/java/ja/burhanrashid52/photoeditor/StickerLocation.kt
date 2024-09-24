package ja.burhanrashid52.photoeditor

import com.rainbow.data.model.memoryspace.Sticker

data class StickerLocation(
    val stickerId: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val width: Int,
    val height: Int,
    val scale: Float,
) {
    fun toSticker() = Sticker(
        stickerId = stickerId.toLong(),
        x = x,
        y = y,
        rotation = rotation,
        width = width,
        height = height,
        scale = scale
    )
}

