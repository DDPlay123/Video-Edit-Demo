package com.side.project.video.utils.trim

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.pm.PackageManager

object DeviceUtil {
    val deviceWidth: Int
        get() = BaseUtils.getContext().resources.displayMetrics.widthPixels
    val deviceHeight: Int
        get() = BaseUtils.getContext().resources.displayMetrics.heightPixels

    fun hasAppInstalled(pkgName: String): Boolean =
        try {
            BaseUtils.getContext().packageManager.getPackageInfo(pkgName, PackageManager.PERMISSION_GRANTED)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    val isAppRunInBackground: Boolean
        get() {
            val activityManager = BaseUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            for (appProcess in appProcesses) {
                if (appProcess.processName == BaseUtils.getContext().packageName) {
                    // return true -> Run in background
                    // return false - > Run in foreground
                    return appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }
            }
            return false
        }
}