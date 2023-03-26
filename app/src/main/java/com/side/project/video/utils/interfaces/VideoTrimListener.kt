package com.side.project.video.utils.interfaces

interface VideoTrimListener {
    fun onStartTrim()
    fun onFinishTrim(url: String?)
    fun onCancel()
}