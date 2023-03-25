package com.side.project.video.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.viewbinding.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.video.utils.Constants.PERMISSION_CODE

/**
 * 常用方法
 */
object Method {
    /**
     * Logcat
     */
    fun logE(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.e(tag, message)
    }

    fun logD(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message)
    }

    /**
     * Permissions
     */
    fun requestPermission(activity: Activity, vararg permissions: String): Boolean {
        return if (!hasPermissions(activity, *permissions)) {
            requestPermissions(activity, permissions, PERMISSION_CODE)
            false
        } else
            true
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                return false
        return true
    }

    /**
     * Tools
     */
    // 將「物件」轉換為「字串」
    inline fun <reified T> toJsonString(data: T): String {
        val type = object : TypeToken<T>() {}.type
        return Gson().toJson(data, type)
    }

    // 將「字串」轉換為「物件」
    inline fun <reified T> fromJsonString(str: String) : T? {
        val type = object : TypeToken<T>() {}.type
        return Gson().fromJson<T>(str, type)
    }
}