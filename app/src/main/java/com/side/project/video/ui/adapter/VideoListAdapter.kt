package com.side.project.video.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.side.project.video.R
import com.side.project.video.databinding.ItemVideoListBinding
import com.side.project.video.ui.adapter.other.BaseRvListAdapter
import com.side.project.video.utils.AsyncImageLoader
import com.side.project.video.utils.Method
import com.side.project.video.utils.VideoItem

class VideoListAdapter : BaseRvListAdapter<ItemVideoListBinding, VideoItem>(R.layout.item_video_list) {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ItemVideoListBinding
        get() = ItemVideoListBinding::inflate

    var onItemClick: ((VideoItem) -> Unit) = {}

    override fun bind(item: VideoItem, binding: ItemVideoListBinding, position: Int) {
        super.bind(item, binding, position)

        binding.apply {
            AsyncImageLoader.loadImage(imgPicture, item.uri)
            tvDuration.text = Method.convertTime(item.duration.toLong())
            root.setOnClickListener { onItemClick(item) }
        }
    }
}