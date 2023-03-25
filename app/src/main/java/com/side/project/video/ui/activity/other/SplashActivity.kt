package com.side.project.video.ui.activity.other

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.side.project.video.ui.activity.MainActivity
import com.side.project.video.utils.Constants
import com.side.project.video.utils.Coroutines
import kotlinx.coroutines.delay

/**
 * 起始畫面
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var splashScreen: SplashScreen

    /**
     * 設定畫面
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_CODE -> {
                for (result in grantResults)
                    if (result != PackageManager.PERMISSION_GRANTED)
                        when {
                            permissions.any { it == Constants.PERMISSION_WRITE_EXTERNAL_STORAGE } -> {
                                return
                            }
                            permissions.any { it == Constants.PERMISSION_MEDIA_IMAGES || it == Constants.PERMISSION_MEDIA_VIDEO } -> {
                                return
                            }
                        }
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        super.onCreate(savedInstanceState)

        doInitialize()
    }

    /**
     * 執行初始化後，跳轉至主畫面
     */
    private fun doInitialize() {
        if (requestMediaPermission())
            startActivity(
                Intent(this, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
    }
}