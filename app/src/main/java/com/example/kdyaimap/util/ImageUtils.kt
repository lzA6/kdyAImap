package com.example.kdyaimap.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/**
 * 图片处理工具类
 * 提供图片压缩、旋转、格式转换等功能
 */
object ImageUtils {
    
    // 图片质量配置
    const val MAX_WIDTH = 1024
    const val MAX_HEIGHT = 1024
    const val QUALITY = 85
    const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    
    /**
     * 压缩图片
     * @param context 上下文
     * @param uri 图片URI
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量 (0-100)
     * @return 压缩后的图片字节数组
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT,
        quality: Int = QUALITY
    ): ByteArray? {
        return try {
            // 获取图片输入流
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                // 解码图片
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                
                // 获取图片尺寸
                BitmapFactory.decodeStream(stream, null, options)
                
                // 计算采样率
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                options.inJustDecodeBounds = false
                
                // 重新打开流（因为之前已经读取过了）
                context.contentResolver.openInputStream(uri)?.use { newStream ->
                    val bitmap = BitmapFactory.decodeStream(newStream, null, options)
                    
                    // 处理图片旋转
                    val rotatedBitmap = handleImageRotation(context, uri, bitmap)
                    
                    // 压缩图片
                    compressBitmap(rotatedBitmap ?: bitmap!!, maxWidth, maxHeight, quality)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 计算图片采样率
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 处理图片旋转
     */
    private fun handleImageRotation(
        context: Context,
        uri: Uri,
        bitmap: Bitmap?
    ): Bitmap? {
        if (bitmap == null) return null
        
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }
                
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle()
                }
                rotatedBitmap
            } ?: bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }
    
    /**
     * 压缩Bitmap
     */
    private fun compressBitmap(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int
    ): ByteArray {
        // 计算目标尺寸
        val (targetWidth, targetHeight) = calculateTargetSize(bitmap.width, bitmap.height, maxWidth, maxHeight)
        
        // 缩放图片
        val scaledBitmap = if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
            val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            if (scaled != bitmap) {
                bitmap.recycle()
            }
            scaled
        } else {
            bitmap
        }
        
        // 压缩为JPEG格式
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        scaledBitmap.recycle()
        
        return outputStream.toByteArray()
    }
    
    /**
     * 计算目标尺寸
     */
    private fun calculateTargetSize(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        var width = originalWidth
        var height = originalHeight
        
        // 计算缩放比例
        val widthRatio = maxWidth.toFloat() / width
        val heightRatio = maxHeight.toFloat() / height
        val ratio = minOf(widthRatio, heightRatio)
        
        if (ratio < 1) {
            width = (width * ratio).toInt()
            height = (height * ratio).toInt()
        }
        
        return Pair(width, height)
    }
    
    /**
     * 保存图片到临时文件
     */
    fun saveImageToTempFile(
        context: Context,
        imageBytes: ByteArray,
        fileName: String? = null
    ): File? {
        return try {
            val tempFile = File(context.cacheDir, fileName ?: "temp_${UUID.randomUUID()}.jpg")
            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(imageBytes)
                outputStream.flush()
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 获取图片文件大小
     */
    fun getImageFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 检查图片文件大小是否超过限制
     */
    fun isImageSizeValid(context: Context, uri: Uri, maxSize: Long = MAX_FILE_SIZE.toLong()): Boolean {
        return getImageFileSize(context, uri) <= maxSize
    }
    
    /**
     * 获取图片格式
     */
    fun getImageFormat(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.getType(uri) ?: "image/jpeg"
        } catch (e: Exception) {
            "image/jpeg"
        }
    }
    
    /**
     * 生成图片文件名
     */
    fun generateImageFileName(originalName: String? = null): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return if (originalName != null) {
            val extension = originalName.substringAfterLast(".", "")
            "img_${timestamp}_${uuid}.$extension"
        } else {
            "img_${timestamp}_${uuid}.jpg"
        }
    }
    
    /**
     * 批量压缩图片
     */
    suspend fun compressImages(
        context: Context,
        uris: List<Uri>,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT,
        quality: Int = QUALITY,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<ByteArray?> {
        val results = mutableListOf<ByteArray?>()
        
        uris.forEachIndexed { index, uri ->
            onProgress(index + 1, uris.size)
            val compressed = compressImage(context, uri, maxWidth, maxHeight, quality)
            results.add(compressed)
        }
        
        return results
    }
    
    /**
     * 验证图片URI
     */
    fun isValidImageUri(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                // 尝试解码图片头部
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(stream, null, options)
                options.outWidth > 0 && options.outHeight > 0
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 图片上传结果
 */
data class ImageUploadResult(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null,
    val fileName: String? = null
)

/**
 * 图片压缩配置
 */
data class ImageCompressionConfig(
    val maxWidth: Int = ImageUtils.MAX_WIDTH,
    val maxHeight: Int = ImageUtils.MAX_HEIGHT,
    val quality: Int = ImageUtils.QUALITY,
    val maxFileSize: Long = ImageUtils.MAX_FILE_SIZE.toLong()
)