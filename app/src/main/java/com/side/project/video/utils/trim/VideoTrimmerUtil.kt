package com.side.project.video.utils.trim

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.composer.Mp4Composer
import com.side.project.video.utils.Method
import com.side.project.video.utils.interfaces.SingleCallback
import com.side.project.video.utils.interfaces.VideoTrimListener
import java.text.SimpleDateFormat
import java.util.*

object VideoTrimmerUtil {
    const val TAG = "VideoTrimmerUtil"
    const val VIDEO_MAX_TIME = 15 // 15秒
    const val MIN_SHOOT_DURATION = 1000L // 最少剪輯時間1s
    const val MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L // 最多剪輯時間15s
    const val MAX_COUNT_RANGE = 15 // seekBar的區間總長度

    val SCREEN_WIDTH_FULL = DeviceUtil.deviceWidth
    val RECYCLER_VIEW_PADDING = UnitConverter.dpToPx(35)
    val VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - VideoTrimmerUtil.RECYCLER_VIEW_PADDING * 2
    val THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VideoTrimmerUtil.VIDEO_MAX_TIME
    val THUMB_HEIGHT = UnitConverter.dpToPx(50)

    /**
     * 公開方法
     */
    fun trim(
        srcPath: String,
        outputPath: String,
        startMs: Long,
        endMs: Long,
        callback: VideoTrimListener
    ) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputName = "trimmedVideo_$timeStamp.mp4"
        val newOutputPath = "$outputPath/$outputName"

        callback.onStartTrim()
        Mp4Composer(srcPath, newOutputPath)
            .trim(startMs, endMs)
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    Method.logE(TAG, "onProgress = $progress")
                }

                override fun onCompleted() {
                    Method.logE(TAG, "onCompleted()")
                    callback.onFinishTrim(newOutputPath)
                }

                override fun onCanceled() {
                    Method.logE(TAG, "onCanceled()")
                }

                override fun onCurrentWrittenVideoTime(timeUs: Long) {
                    Method.logE(TAG, "onCurrentWrittenVideoTime = $timeUs")
                }

                override fun onFailed(exception: Exception) {
                    Method.logE(TAG, "onFailed(${exception.message})")
                }
            })
            .start()
    }

    fun shootVideoThumbInBackground(
        context: Context?, videoUri: Uri?, totalThumbsCount: Int, startPosition: Long,
        endPosition: Long, callback: SingleCallback<Bitmap?, Int?>
    ) {
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, videoUri)
                    // Retrieve media data use microsecond
                    val interval = (endPosition - startPosition) / (totalThumbsCount - 1)
                    for (i in 0 until totalThumbsCount) {
                        val frameTime = startPosition + interval * i
                        var bitmap: Bitmap? = mediaMetadataRetriever.getFrameAtTime(
                            frameTime * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                            ?: continue
                        try {
                            bitmap = bitmap?.let {
                                Bitmap.createScaledBitmap(it, THUMB_WIDTH, THUMB_HEIGHT, false)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                        callback.onSingleCallback(bitmap, interval.toInt())
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(Thread.currentThread(), e)
                }
            }
        })
    }

    /**
     * 私有方法
     */
    private fun convertSecondsToTime(seconds: Long): String {
        val timeStr: String?
        val hour: Int
        var minute: Int
        val second: Int
        if (seconds <= 0) {
            return "00:00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second)
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }

    private fun unitFormat(i: Int): String =
        if (i in 0..9) "0$i" else "" + i
}