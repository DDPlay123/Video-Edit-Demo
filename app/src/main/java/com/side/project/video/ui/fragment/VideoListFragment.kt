package com.side.project.video.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.side.project.video.R
import com.side.project.video.databinding.FragmentVideoListBinding
import com.side.project.video.ui.adapter.VideoListAdapter
import com.side.project.video.ui.fragment.VideoEditFragment.Companion.Arg_Video_Item
import com.side.project.video.ui.fragment.other.BaseFragment
import com.side.project.video.utils.*

class VideoListFragment : BaseFragment<FragmentVideoListBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVideoListBinding
        get() = FragmentVideoListBinding::inflate

    /**
     * 參數
     */
    private lateinit var videoListAdapter: VideoListAdapter

    /**
     * Lifecycle
     */
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
            icon.setImageResource(R.mipmap.ic_icon_foreground)
            setTitle(getString(R.string.title_video_list))
        }
    }

    private fun doInitialize() {
        videoListAdapter = VideoListAdapter()
        with(binding?.rvGrid ?: return) {
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            adapter = videoListAdapter
            videoListAdapter
        }.apply {
            onItemClick = { item ->
                with(Bundle()) {
                    putString(Arg_Video_Item, Method.toJsonString<VideoItem>(item))
                    findNavController().navigate(R.id.action_videoListFragment_to_videoEditFragment, this)
                }
            }
        }.also { queryVideoList() }
    }

    private fun queryVideoList() {
        VideoUtils.getAllShownVideoRes(mActivity).let { list ->
            videoListAdapter.submitList(list.toMutableList())
        }
    }
}