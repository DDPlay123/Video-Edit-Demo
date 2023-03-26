package com.side.project.video.utils.trim

import android.util.DisplayMetrics

object UnitConverter {
    val displayMetrics: DisplayMetrics
        get() = BaseUtils.getContext().resources.displayMetrics

    fun dpToPx(dp: Float): Float =
        dp * displayMetrics.density

    fun dpToPx(dp: Int): Int =
        (dp * displayMetrics.density + 0.5f).toInt()

    fun pxToDp(px: Float): Float =
        px / displayMetrics.density

    fun pxToDp(px: Int): Int =
        (px / displayMetrics.density + 0.5f).toInt()

    fun spToPx(sp: Float): Float =
        sp * displayMetrics.scaledDensity

    fun spToPx(sp: Int): Int =
        (sp * displayMetrics.scaledDensity + 0.5f).toInt()

    fun pxToSp(px: Float): Float =
        px / displayMetrics.scaledDensity

    fun pxToSp(px: Int): Int =
        (px / displayMetrics.scaledDensity + 0.5f).toInt()
}