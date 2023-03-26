package com.side.project.video.utils

import android.app.Application
import com.side.project.video.utils.trim.BaseUtils

class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        BaseUtils.init(this)
    }
}