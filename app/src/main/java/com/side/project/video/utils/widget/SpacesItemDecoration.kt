package com.side.project.video.utils.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacesItemDecoration(private val space: Int, private val thumbnailsCount: Int)
    : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.left = space
            outRect.right = 0
        } else if (thumbnailsCount > 15 && position == thumbnailsCount - 1) {
            outRect.left = 0
            outRect.right = space
        } else {
            outRect.left = 0
            outRect.right = 0
        }
    }
}