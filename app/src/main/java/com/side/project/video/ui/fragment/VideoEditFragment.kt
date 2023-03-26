package com.side.project.video.ui.fragment

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.side.project.video.R
import com.side.project.video.databinding.FragmentVideoEditBinding
import com.side.project.video.ui.adapter.ThumbnailAdapter
import com.side.project.video.ui.fragment.VideoPreviewFragment.Companion.Arg_Video_Path
import com.side.project.video.ui.fragment.other.BaseFragment
import com.side.project.video.utils.*
import com.side.project.video.utils.interfaces.SingleCallback
import com.side.project.video.utils.interfaces.VideoTrimListener
import com.side.project.video.utils.trim.BackgroundExecutor
import com.side.project.video.utils.trim.StorageUtil
import com.side.project.video.utils.trim.UiThreadExecutor
import com.side.project.video.utils.trim.VideoTrimmerUtil
import com.side.project.video.utils.trim.VideoTrimmerUtil.MAX_COUNT_RANGE
import com.side.project.video.utils.trim.VideoTrimmerUtil.MAX_SHOOT_DURATION
import com.side.project.video.utils.trim.VideoTrimmerUtil.RECYCLER_VIEW_PADDING
import com.side.project.video.utils.trim.VideoTrimmerUtil.THUMB_WIDTH
import com.side.project.video.utils.trim.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH
import com.side.project.video.utils.widget.RangeSeekBarView
import com.side.project.video.utils.widget.SpacesItemDecoration
import kotlin.math.abs

class VideoEditFragment : BaseFragment<FragmentVideoEditBinding>() {
    companion object {
        const val Tag = "VideoEditFragment"
        const val Arg_Video_Item = "Arg_Video_Item"
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVideoEditBinding
        get() = FragmentVideoEditBinding::inflate

    /**
     * 參數
     */
    // 傳值
    private lateinit var videoItem: VideoItem

    // 底部縮圖及進度條
    private var mProgressDialog: ProgressDialog? = null
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    private lateinit var mRangeSeekBarView: RangeSeekBarView
    private var mDuration = 0 // 影片總長度
    private var mAverageMsPx = 0f // 每毫秒所占的px
    private var averagePxMs = 0f // 每px所佔用的ms毫秒
    private var isFromRestore = false // 是否從恢復狀態
    private var mLeftProgressPos: Long = 0 // 左邊進度條位置
    private var mRightProgressPos: Long = 0 // 右邊進度條位置
    private var mRedProgressBarPos: Long = 0 // 紅色進度條位置
    private var scrollPos: Long = 0 // 滾動位置
    private var mScaledTouchSlop = 0 // 滑動最小距離
    private var lastScrollX = 0 // 上次滑動的位置
    private var isSeeking = false // 是否正在拖動
    private var isOverScaledTouchSlop = false // 是否超過最小滑動距離
    private var mThumbsTotalCount = 0 // 總共有幾個縮圖
    private var mRedProgressAnimator: ValueAnimator? = null // 紅色進度條動畫
    private var mAnimationHandler = Handler(Looper.getMainLooper()) // 動畫Handler

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
        doInitialize()
        setListener()
    }

    override fun onPause() {
        super.onPause()
        onVideoPause()
        isFromRestore = true
    }

    override fun FragmentVideoEditBinding.destroy() {
        if (mRedProgressAnimator != null && mRedProgressAnimator?.isRunning == true) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable)
            mRedProgressAnimator?.cancel()
        }
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    /**
     * 執行動作
     */
    private fun initVideoByURI() {
        binding?.run {
            videoView.setVideoURI(Uri.parse(VideoUtils.getPath(mActivity, Uri.parse(videoItem.uri))))
            videoView.requestFocus()
            tvShootTip.text = String.format(getString(R.string.text_drag), VideoTrimmerUtil.VIDEO_MAX_TIME)
        }
    }

    private fun setActionBar() {
        binding?.actionBar?.apply {
            icon.setOnClickListener {
                mOnTrimVideoListener.onCancel()
                mActivity.onBackPressed()
            }
            setTitle(getString(R.string.title_video_edit))
            setImage(1, R.drawable.baseline_check_24)?.setOnClickListener {
                Method.logE(Tag, "Save")
                if (mRightProgressPos - mLeftProgressPos < VideoTrimmerUtil.MIN_SHOOT_DURATION) {
                    Toast.makeText(mActivity, getString(R.string.text_last_trim), Toast.LENGTH_SHORT).show()
                } else {
                    binding?.videoView?.pause()
                    VideoTrimmerUtil.trim(
                        VideoUtils.getPath(mActivity, Uri.parse(videoItem.uri)).toString(),
                        StorageUtil.cacheDir,
                        mLeftProgressPos,
                        mRightProgressPos,
                        mOnTrimVideoListener
                    )
                }
            }
        }
    }

    private fun doInitialize() {
        binding?.run {
            thumbnailAdapter = ThumbnailAdapter()
            rvThumbnails.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = thumbnailAdapter
                addOnScrollListener(mOnScrollListener)
            }.also { initVideoByURI() }
        }
    }

    private fun videoPrepared(mp: MediaPlayer) {
        binding?.run {
            val lp: ViewGroup.LayoutParams = videoView.layoutParams
            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenWidth: Int = layoutVideo.width
            val screenHeight: Int = layoutVideo.height

            if (videoHeight > videoWidth) {
                lp.width = screenWidth
                lp.height = screenHeight
            } else {
                lp.width = screenWidth
                val r = videoHeight / videoWidth.toFloat()
                lp.height = (lp.width * r).toInt()
            }
            videoView.layoutParams = lp
            mDuration = videoView.duration
            if (!isFromRestore) {
                seekTo(mRedProgressBarPos)
            } else {
                isFromRestore = false
                seekTo(mRedProgressBarPos)
            }
            initRangeSeekBarView()
            startShootVideoThumbs(mActivity, Uri.parse(videoItem.uri), mThumbsTotalCount, 0, mDuration.toLong())
        }
    }

    private fun initRangeSeekBarView() {
        binding?.run {
            if (::mRangeSeekBarView.isInitialized) return
            mLeftProgressPos = 0
            if (mDuration <= MAX_SHOOT_DURATION) {
                mThumbsTotalCount = MAX_COUNT_RANGE
                mRightProgressPos = mDuration.toLong()
            } else {
                mThumbsTotalCount = ((mDuration * 1.0f / (MAX_SHOOT_DURATION * 1.0f) * MAX_COUNT_RANGE).toInt())
                mRightProgressPos = MAX_SHOOT_DURATION
            }
            rvThumbnails.addItemDecoration(SpacesItemDecoration(RECYCLER_VIEW_PADDING, mThumbsTotalCount))
            mRangeSeekBarView = RangeSeekBarView(mActivity, mLeftProgressPos, mRightProgressPos)
            mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos)
            mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos)
            mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
            mRangeSeekBarView.setMinShootTime(VideoTrimmerUtil.MIN_SHOOT_DURATION)
            mRangeSeekBarView.setNotifyWhileDragging(true)
            mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)
            seekBarLayout.addView(mRangeSeekBarView)
            mAverageMsPx = if (mThumbsTotalCount - MAX_COUNT_RANGE > 0)
                (mDuration - MAX_SHOOT_DURATION) / (mThumbsTotalCount - MAX_COUNT_RANGE).toFloat()
            else 0f
            averagePxMs = VIDEO_FRAMES_WIDTH * 1.0f / (mRightProgressPos - mLeftProgressPos)
        }
    }

    private fun startShootVideoThumbs(
        context: Context,
        videoUri: Uri,
        totalThumbsCount: Int,
        startPosition: Long,
        endPosition: Long
    ) {
        VideoTrimmerUtil.shootVideoThumbInBackground(context,
            videoUri,
            totalThumbsCount,
            startPosition,
            endPosition,
            object : SingleCallback<Bitmap?, Int?> {
                override fun onSingleCallback(bitmap: Bitmap?, interval: Int?) {
                    if (bitmap != null)
                        UiThreadExecutor.runTask("", { thumbnailAdapter.addThumbnail(bitmap) }, 0L)
                }
            })
    }

    private fun setListener() {
        binding?.run {
            videoView.setOnPreparedListener { mp ->
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                videoPrepared(mp)
            }

            videoView.setOnCompletionListener {
                seekTo(mLeftProgressPos)
                setPlayPauseViewIcon(false)
            }

            imgVideoPlay.setOnClickListener {
                mRedProgressBarPos = videoView.currentPosition.toLong()
                if (videoView.isPlaying) {
                    videoView.pause()
                    pauseRedProgressAnimation()
                } else {
                    videoView.start()
                    playingRedProgressAnimation()
                }
                setPlayPauseViewIcon(videoView.isPlaying)
            }
        }
    }

    private fun seekTo(mSec: Long) {
        binding?.videoView?.seekTo(mSec.toInt())
        Method.logE(Tag, "seekTo = $mSec")
    }

    private fun setPlayPauseViewIcon(isPlaying: Boolean) {
        binding?.imgVideoPlay?.setImageResource(
            if (isPlaying) R.drawable.img_video_pause_black else R.drawable.img_video_play_black
        )
    }

    private fun pauseRedProgressAnimation() {
        binding?.positionIcon?.clearAnimation()
        if (mRedProgressAnimator != null && mRedProgressAnimator?.isRunning == true) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable)
            mRedProgressAnimator?.cancel()
        }
    }

    private fun playingRedProgressAnimation() {
        pauseRedProgressAnimation()
        playingAnimation()
        mAnimationHandler.post(mAnimationRunnable)
    }

    private fun playingAnimation() {
        binding?.run {
            if (positionIcon.visibility == View.GONE) positionIcon.display()
            val params = positionIcon.layoutParams as FrameLayout.LayoutParams
            val start = (RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs).toInt()
            val end = (RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs).toInt()
            mRedProgressAnimator = ValueAnimator.ofInt(start, end)
                .setDuration(mRightProgressPos - scrollPos - (mRedProgressBarPos - scrollPos))
            mRedProgressAnimator?.interpolator = LinearInterpolator()
            mRedProgressAnimator?.addUpdateListener { animation ->
                params.leftMargin = animation.animatedValue as Int
                positionIcon.layoutParams = params
                Method.logE(Tag, "----onAnimationUpdate--->>>>>>>$mRedProgressBarPos")
            }
            mRedProgressAnimator?.start()
        }
    }

    private fun onVideoPause() {
        binding?.run {
            if (videoView.isPlaying) {
                seekTo(mLeftProgressPos) // 復位
                videoView.pause()
                setPlayPauseViewIcon(false)
                positionIcon.gone()
            }
        }
    }

    /**
     * 其他Listener
     */
    private val mOnTrimVideoListener = object : VideoTrimListener {
        override fun onStartTrim() {
            buildDialog(getString(R.string.text_trim_start))
        }

        override fun onFinishTrim(url: String?) {
            Coroutines.main {
                if (mProgressDialog?.isShowing == true) mProgressDialog?.dismiss()
                Toast.makeText(mActivity, getString(R.string.text_trim_end), Toast.LENGTH_SHORT).show()
                Method.logE(Tag, url.toString())
                with(Bundle()) {
                    putString(Arg_Video_Path, url.toString())
                    findNavController().navigate(R.id.action_videoEditFragment_to_videoPreviewFragment, this)
                }
            }
        }

        override fun onCancel() {
            onDestroyView()
        }
    }

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            Method.logE(Tag, "onScrollStateChanged newState = $newState")
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            binding?.run {
                isSeeking = false
                val scrollX: Int = calcScrollXDistance()
                // 達不到滑動的距離
                if (abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                    isOverScaledTouchSlop = false
                    return
                }
                isOverScaledTouchSlop = true
                // 初始狀態,why ? 因為默認的時候有35dp的空白！
                if (scrollX == -RECYCLER_VIEW_PADDING) {
                    scrollPos = 0
                    mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos
                    mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos
                    Method.logE(Tag, "onScrolled >>>> mLeftProgressPos = $mLeftProgressPos")
                    mRedProgressBarPos = mLeftProgressPos
                } else {
                    isSeeking = true
                    scrollPos = (mAverageMsPx * (RECYCLER_VIEW_PADDING + scrollX) / THUMB_WIDTH).toLong()
                    mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos
                    mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos
                    Method.logE(Tag, "onScrolled >>>> mLeftProgressPos = $mLeftProgressPos")
                    mRedProgressBarPos = mLeftProgressPos
                    if (videoView.isPlaying) {
                        videoView.pause()
                        setPlayPauseViewIcon(false)
                    }
                    positionIcon.gone()
                    seekTo(mLeftProgressPos)
                    mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
                    mRangeSeekBarView.invalidate()
                }
                lastScrollX = scrollX
            }
        }
    }

    // 水平滑動了多少px
    private fun calcScrollXDistance(): Int {
        val layoutManager = binding?.rvThumbnails?.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView!!.width
        return position * itemWidth - firstVisibleChildView.left
    }

    private val mOnRangeSeekBarChangeListener = object : RangeSeekBarView.OnRangeSeekBarChangeListener {
        override fun onRangeSeekBarValuesChanged(
            bar: RangeSeekBarView?,
            minValue: Long,
            maxValue: Long,
            action: Int,
            isMin: Boolean,
            pressedThumb: RangeSeekBarView.Thumb?
        ) {
            Method.logE(Tag, "-----minValue----->>>>>>$minValue")
            Method.logE(Tag, "-----maxValue----->>>>>>$maxValue")
            mLeftProgressPos = minValue + scrollPos
            mRedProgressBarPos = mLeftProgressPos
            mRightProgressPos = maxValue + scrollPos
            Method.logE(Tag, "-----mLeftProgressPos----->>>>>>$mLeftProgressPos")
            Method.logE(Tag, "-----mRightProgressPos----->>>>>>$mRightProgressPos")
            when (action) {
                MotionEvent.ACTION_DOWN -> isSeeking = false

                MotionEvent.ACTION_MOVE -> {
                    isSeeking = true
                    mRedProgressBarPos = mLeftProgressPos
                    pauseRedProgressAnimation()
                    onVideoPause()
                    seekTo((if (pressedThumb == RangeSeekBarView.Thumb.MIN) mLeftProgressPos else mRightProgressPos))
                }

                MotionEvent.ACTION_UP -> {
                    isSeeking = false
                    seekTo(mLeftProgressPos)
                }

                else -> Unit
            }

            mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        }
    }

    /**
     * 其他
     */
    private var mAnimationRunnable: Runnable = Runnable { updateVideoProgress() }

    private fun updateVideoProgress() {
        binding?.run {
            val currentPosition: Long = videoView.currentPosition.toLong()
            Method.logE(Tag, "updateVideoProgress currentPosition = $currentPosition")
            if (currentPosition >= mRightProgressPos || currentPosition == 0L) {
                mRedProgressBarPos = mLeftProgressPos
                pauseRedProgressAnimation()
                onVideoPause()
            } else {
                mAnimationHandler.post(mAnimationRunnable)
            }
        }
    }

    private fun buildDialog(msg: String): ProgressDialog? {
        if (mProgressDialog == null) mProgressDialog = ProgressDialog.show(mActivity, "", msg)
        mProgressDialog?.setMessage(msg)
        return mProgressDialog
    }
}