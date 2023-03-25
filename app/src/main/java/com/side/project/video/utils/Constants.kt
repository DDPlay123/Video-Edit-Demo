package com.side.project.video.utils

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 常數變量
 */
object Constants {
    /**
     * Permission
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    const val PERMISSION_MEDIA_IMAGES = android.Manifest.permission.READ_MEDIA_IMAGES
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    const val PERMISSION_MEDIA_VIDEO = android.Manifest.permission.READ_MEDIA_VIDEO
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE

    val media_permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(PERMISSION_MEDIA_IMAGES, PERMISSION_MEDIA_VIDEO)
    else
        arrayOf(PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE)

    /**
     * Permission Code
     */
    const val PERMISSION_CODE = 1001
}