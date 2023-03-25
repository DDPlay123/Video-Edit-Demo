package com.side.project.video.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.side.project.video.R
import com.side.project.video.databinding.FragmentVideoEditBinding
import com.side.project.video.ui.fragment.other.BaseFragment
import com.side.project.video.utils.Method
import com.side.project.video.utils.VideoItem

class VideoEditFragment : BaseFragment<FragmentVideoEditBinding>() {
    companion object {
        const val Arg_Video_Item = "Arg_Video_Item"
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVideoEditBinding
        get() = FragmentVideoEditBinding::inflate

    /**
     * 參數
     */
    private lateinit var videoItem: VideoItem

    /**
     * Lifecycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoItem = Method.fromJsonString<VideoItem>(it.getString(Arg_Video_Item) ?: "") ?: VideoItem()
            if (videoItem.id == -1L) mActivity.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setActionBar()
    }

    /**
     * 執行動作
     */
    private fun setActionBar() {
        binding?.actionBar?.apply {
            icon.setOnClickListener { mActivity.onBackPressed() }
            setTitle(getString(R.string.title_video_edit))
        }
    }
}