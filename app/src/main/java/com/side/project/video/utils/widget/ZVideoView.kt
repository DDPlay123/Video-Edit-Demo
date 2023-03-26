package com.side.project.video.utils.widget

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.widget.VideoView

class ZVideoView : VideoView {
    private val mVideoWidth = 480
    private val mVideoHeight = 480
    private var videoRealW = 1
    private var videoRealH = 1

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setVideoURI(uri: Uri) {
        super.setVideoURI(uri)
        val retr = MediaMetadataRetriever()
        retr.setDataSource(uri.path)
        val height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
        val width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        try {
            videoRealH = height ?: 0
            videoRealW = width ?: 0
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    override fun setVideoPath(path: String?) {
        super.setVideoPath(path)
        val retr = MediaMetadataRetriever()
        retr.setDataSource(path)
        val height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
        val width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        try {
            videoRealH = height ?: 0
            videoRealW = width ?: 0
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }
}