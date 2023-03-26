package com.side.project.video.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.side.project.video.R
import com.side.project.video.databinding.FragmentVideoEditBinding
import com.side.project.video.ui.adapter.ThumbnailAdapter
import com.side.project.video.ui.fragment.other.BaseFragment
import com.side.project.video.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoEditFragment : BaseFragment<FragmentVideoEditBinding>() {
    companion object {
        const val Tag = "VideoEditFragment"
        const val Arg_Video_Item = "Arg_Video_Item"
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVideoEditBinding
        get() = FragmentVideoEditBinding::inflate

    private var retriever: MediaMetadataRetriever? = null
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    private var thumbnailCount: Int = 0

    /**
     * 參數
     */
    private lateinit var videoItem: VideoItem

    private var player: ExoPlayer? = null
    private var isVideoEnded: Boolean = false

    /**
     * Lifecycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoItem = Method.fromJsonString<VideoItem>(it.getString(Arg_Video_Item) ?: "") ?: VideoItem()
            if (videoItem.id == -1L) mActivity.onBackPressed()
            thumbnailCount = videoItem.duration.toInt() / 1000
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setActionBar()
        initializeRv()
        initializePlayer()
        setListener()
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun FragmentVideoEditBinding.destroy() {
        player?.let {
            it.stop()
            it.release()
            player = null
        }
        retriever?.release()
        retriever = null
    }

    /**
     * 執行動作
     */
    private fun setActionBar() {
        binding?.actionBar?.apply {
            icon.setOnClickListener { mActivity.onBackPressed() }
            setTitle(getString(R.string.title_video_edit))
            setImage(1, R.drawable.baseline_check_24)?.setOnClickListener {
                Method.logE(Tag, "Save")
            }
        }
    }

    private fun initializePlayer() {
        binding?.apply {
            player = ExoPlayer.Builder(mActivity).build()
            // create a media item.
            val mediaItem = MediaItem.Builder()
                .setUri(videoItem.uri)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()

            // Create a media source and pass the media item
            val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(mActivity))
                .createMediaSource(mediaItem)

            // Finally assign this media source to the player
            player?.apply {
                setMediaSource(mediaSource)
                imgPlayer.display()
                pbCircular.gone()
                playWhenReady = true // start playing when the exoplayer has setup
                seekTo(0, 0L) // Start from the beginning
                prepare() // Change the state from idle.
                addListener(object : Player.Listener {
                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)
                        if (playWhenReady) imgPlayer.gone() else imgPlayer.display()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                Method.logE(Tag, "STATE_BUFFERING")
                            }

                            Player.STATE_READY -> {
                                Method.logE(Tag, "STATE_READY")
                                imgPlayer.gone()
                                isVideoEnded = false
                            }

                            Player.STATE_ENDED -> {
                                Method.logE(Tag, "STATE_ENDED")
                                isVideoEnded = true
                            }

                            Player.STATE_IDLE -> {
                                Method.logE(Tag, "STATE_IDLE")
                            }

                            else -> Unit
                        }
                    }
                })
            }.also {
                // Do not forget to attach the player to the view
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                playerView.player = it
            }
        }
    }

    private fun initializeRv() {
        thumbnailAdapter = ThumbnailAdapter(thumbnailCount)
        binding?.rvThumbnails?.apply {
            layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = thumbnailAdapter
        }.also {
            retriever = MediaMetadataRetriever()
            retriever?.setDataSource(context, Uri.parse(videoItem.uri))
            Coroutines.io {
                // 提取每秒鐘的縮略圖並添加到Adapter中
                for (i in 0 until thumbnailCount) {
                    val time = i * 1000000L // 時間戳，單位為微秒
                    val thumbnail: Bitmap = retriever?.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: return@io
                    withContext(Dispatchers.Main) {
                        Method.logE(Tag, "thumbnail$i: $thumbnail")
                        thumbnailAdapter.addThumbnail(thumbnail, i)
                    }
                }
                retriever?.release()
            }
        }
    }

    private fun setListener() {
        binding?.run {
            imgPlayer.setOnClickListener { onVideoClick() }

            playerView.setOnClickListener { onVideoClick() }
        }
    }

    private fun onVideoClick() {
        binding?.apply {
            try {
                playerView.player?.apply {
                    if (isVideoEnded) {
                        seekTo(0)
                        playWhenReady = true
                        return
                    }

                    playWhenReady = !playWhenReady
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Method.logE(Tag, "Error: ${e.message}")
            }
        }
    }
}