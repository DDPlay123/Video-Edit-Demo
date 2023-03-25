package com.side.project.video.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.side.project.video.R
import com.side.project.video.databinding.ItemVideoListBinding
import com.side.project.video.ui.adapter.other.BaseRvListAdapter
import com.side.project.video.utils.AsyncImageLoader
import com.side.project.video.utils.VideoItem

class VideoListAdapter : BaseRvListAdapter<ItemVideoListBinding, VideoItem>(R.layout.item_video_list) {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ItemVideoListBinding
        get() = ItemVideoListBinding::inflate

    var onItemClick: ((VideoItem) -> Unit) = {}

    override fun bind(item: VideoItem, binding: ItemVideoListBinding, position: Int) {
        super.bind(item, binding, position)

        binding.apply {
            AsyncImageLoader.loadImage(imgPicture, item.uri)
            tvDuration.text = convertTime(item.duration)
            root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun convertTime(duration: String): String {
        try {
            val seconds: Int
            val minutes: Int
            val hours: Int
            var x: Int = duration.toInt() / 1000
            seconds = x % 60
            x /= 60
            minutes = x % 60
            x /= 60
            hours = x % 24
            return if (hours != 0)
                String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
            else
                String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
        } catch (ignored: Exception) {
            return "00:00"
        }
    }
}