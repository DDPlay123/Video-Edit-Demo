package com.side.project.video.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.navigation.findNavController
import com.side.project.video.R
import com.side.project.video.databinding.FragmentVideoPreviewBinding
import com.side.project.video.ui.fragment.other.BaseFragment

class VideoPreviewFragment : BaseFragment<FragmentVideoPreviewBinding>() {
    companion object {
        const val Tag = "VideoPreviewFragment"
        const val Arg_Video_Path = "Arg_Video_Path"
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVideoPreviewBinding
        get() = FragmentVideoPreviewBinding::inflate

    private var videoPath: String? = null

    /**
     * Lifecycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoPath = it.getString(Arg_Video_Path)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setActionBar()
        doInitialize()
    }

    /**
     * 執行動作
     */
    private fun setActionBar() {
        binding?.actionBar?.apply {
            icon.setOnClickListener { findNavController().navigate(R.id.action_videoPreviewFragment_to_videoListFragment) }
            setTitle(getString(R.string.title_video_preview))
        }
    }

    private fun doInitialize() {
        binding?.videoView?.apply {
            setVideoPath(videoPath)
            setOnPreparedListener { start() }

            val mediaController = MediaController(mActivity)
            setMediaController(mediaController)
            mediaController.setAnchorView(this)
        }
    }
}