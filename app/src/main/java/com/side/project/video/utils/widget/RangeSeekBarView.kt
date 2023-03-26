package com.side.project.video.utils.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.side.project.video.R
import com.side.project.video.utils.getColorCompat
import com.side.project.video.utils.trim.DateUtil
import com.side.project.video.utils.trim.UnitConverter
import com.side.project.video.utils.trim.VideoTrimmerUtil
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.roundToInt

class RangeSeekBarView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object {
        private const val TAG = "RangeSeekBarView"
        private const val INVALID_POINTER_ID = 255
        private const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        private const val ACTION_POINTER_INDEX_SHIFT = 8
        private val TextPositionY: Int = UnitConverter.dpToPx(7)
        private val paddingTop: Int = UnitConverter.dpToPx(10)
    }

    private var mActivePointerId = INVALID_POINTER_ID
    private var mMinShootTime: Long = VideoTrimmerUtil.MIN_SHOOT_DURATION
    private var absoluteMinValuePrim = 0.0
    private  var absoluteMaxValuePrim: Double = 0.0
    private var normalizedMinValue = 0.0 // 坐標佔總長度的比例值，範圍從0-1
    private var normalizedMaxValue = 1.0 // 點坐標佔總長度的比例值，範圍從0-1
    private var normalizedMinValueTime = 0.0
    private var normalizedMaxValueTime = 1.0 // normalized：規格化的--點坐標佔總長度的比例值，範圍從0-1

    private var mScaledTouchSlop = 0
    private lateinit var thumbImageLeft: Bitmap
    private lateinit var thumbImageRight: Bitmap
    private lateinit var thumbPressedImage: Bitmap
    private lateinit var paint: Paint
    private lateinit var rectPaint: Paint

    private var mVideoTrimTimePaintL = Paint()
    private var mVideoTrimTimePaintR = Paint()
    private var mShadow = Paint()

    private var thumbWidth = 0
    private var thumbHalfWidth = 0f
    private var padding = 0f
    private var mStartPosition: Long = 0
    private var mEndPosition: Long = 0
    private var thumbPaddingTop = 0f
    private var isTouchDown = false
    private var mDownMotionX = 0f
    private var mIsDragging = false

    private var pressedThumb: Thumb? = null
    private var isMin = false
    private var minWidth = 1.0 // 最小裁剪距離

    private var notifyWhileDragging = false
    private var mRangeSeekBarChangeListener: OnRangeSeekBarChangeListener? = null
    private var whiteColorRes = context.getColorCompat(R.color.seek_bar)

    enum class Thumb { MIN, MAX }

    constructor(context: Context, absoluteMinValuePrim: Long, absoluteMaxValuePrim: Long) : super(context) {
        this.absoluteMinValuePrim = absoluteMinValuePrim.toDouble()
        this.absoluteMaxValuePrim = absoluteMaxValuePrim.toDouble()
        isFocusable = true
        isFocusableInTouchMode = true
        init()
    }

    private fun init() {
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        thumbImageLeft = BitmapFactory.decodeResource(resources, R.drawable.img_video_thumb_handle2)

        val width = thumbImageLeft.width
        val height = thumbImageLeft.height
        val newWidth = UnitConverter.dpToPx(15)
        val newHeight = UnitConverter.dpToPx(75)
        val scaleWidth = newWidth * 1.0f / width
        val scaleHeight = newHeight * 1.0f / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        thumbImageLeft = Bitmap.createBitmap(thumbImageLeft, 0, 0, width, height, matrix, true)
        thumbImageRight = thumbImageLeft
        thumbPressedImage = thumbImageLeft
        thumbWidth = newWidth
        thumbHalfWidth = (thumbWidth / 2).toFloat()
        val shadowColor = context.getColorCompat(R.color.black_000000_70)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = whiteColorRes

        mVideoTrimTimePaintL.strokeWidth = 3f
        mVideoTrimTimePaintL.setARGB(255, 51, 51, 51)
        mVideoTrimTimePaintL.textSize = 28f
        mVideoTrimTimePaintL.isAntiAlias = true
        mVideoTrimTimePaintL.color = whiteColorRes
        mVideoTrimTimePaintL.textAlign = Paint.Align.LEFT

        mVideoTrimTimePaintR.strokeWidth = 3f
        mVideoTrimTimePaintR.setARGB(255, 51, 51, 51)
        mVideoTrimTimePaintR.textSize = 28f
        mVideoTrimTimePaintR.isAntiAlias = true
        mVideoTrimTimePaintR.color = whiteColorRes
        mVideoTrimTimePaintR.textAlign = Paint.Align.RIGHT
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 300
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec))
            width = MeasureSpec.getSize(widthMeasureSpec)

        var height = 120
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec))
            height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bgMiddleLeft = 0f
        val bgMiddleRight = (width - paddingRight).toFloat()
        val rangeL: Float = normalizedToScreen(normalizedMinValue)
        val rangeR: Float = normalizedToScreen(normalizedMaxValue)
        val leftRect = Rect(bgMiddleLeft.toInt(), height, rangeL.toInt(), 0)
        val rightRect = Rect(rangeR.toInt(), height, bgMiddleRight.toInt(), 0)
        canvas.drawRect(leftRect, mShadow)
        canvas.drawRect(rightRect, mShadow)
        canvas.drawRect(rangeL, thumbPaddingTop + RangeSeekBarView.paddingTop, rangeR,
            thumbPaddingTop + UnitConverter.dpToPx(2) + RangeSeekBarView.paddingTop, rectPaint)
        canvas.drawRect(rangeL, (height - UnitConverter.dpToPx(2)).toFloat(), rangeR, height.toFloat(), rectPaint)
        drawThumb(normalizedToScreen(normalizedMinValue), false, canvas, true)
        drawThumb(normalizedToScreen(normalizedMaxValue), false, canvas, false)
        drawVideoTrimTimeText(canvas)
    }

    private fun drawThumb(screenCoord: Float, pressed: Boolean, canvas: Canvas, isLeft: Boolean) {
        canvas.drawBitmap(
            if (pressed) thumbPressedImage else if (isLeft) thumbImageLeft else thumbImageRight,
            screenCoord - if (isLeft) 0 else thumbWidth,
            RangeSeekBarView.paddingTop.toFloat(), paint
        )
    }

    private fun drawVideoTrimTimeText(canvas: Canvas) {
        val leftThumbsTime: String = DateUtil.convertSecondsToTime(mStartPosition)
        val rightThumbsTime: String = DateUtil.convertSecondsToTime(mEndPosition)
        canvas.drawText(
            leftThumbsTime,
            normalizedToScreen(normalizedMinValue),
            TextPositionY.toFloat(),
            mVideoTrimTimePaintL
        )
        canvas.drawText(
            rightThumbsTime,
            normalizedToScreen(normalizedMaxValue),
            TextPositionY.toFloat(),
            mVideoTrimTimePaintR
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTouchDown)
            return super.onTouchEvent(event)
        if (event.pointerCount > 1)
            return super.onTouchEvent(event)
        if (!isEnabled) return false
        if (absoluteMaxValuePrim <= mMinShootTime)
            return super.onTouchEvent(event)

        val pointerIndex: Int // 記錄點擊點的index
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // 記住最後一個手指點擊屏幕的點的坐標x，mDownMotionX
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                // 判斷touch到的是最大值thumb還是最小值thumb
                pressedThumb = evalPressedThumb(mDownMotionX)
                if (pressedThumb == null) return super.onTouchEvent(event)
                isPressed = true // 設置該控件被按下了
                onStartTrackingTouch() // 置mIsDragging為true，開始追踪touch事件
                trackTouchEvent(event)
                attemptClaimDrag()
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener?.onRangeSeekBarValuesChanged(
                        this,
                        getSelectedMinValue(),
                        getSelectedMaxValue(),
                        MotionEvent.ACTION_DOWN,
                        isMin,
                        pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {
                if (mIsDragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex) // 手指在控件上點的X坐標
                    // 手指沒有點在最大最小值上，並且在控件上有滑動事件
                    if (abs(x - mDownMotionX) > mScaledTouchSlop) {
                        isPressed = true
                        Log.e(TAG, "没有拖住最大最小值") // 一直不會執行？
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                if (notifyWhileDragging && mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener?.onRangeSeekBarValuesChanged(
                        this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_MOVE,
                        isMin, pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                invalidate()
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener?.onRangeSeekBarValuesChanged(
                        this,
                        getSelectedMinValue(),
                        getSelectedMaxValue(),
                        MotionEvent.ACTION_UP,
                        isMin,
                        pressedThumb
                    )
                }
                pressedThumb = null // 手指抬起，則置被touch到的thumb為空
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
            else -> {}
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 1) return
        Log.e(TAG, "trackTouchEvent: " + event.action + " x: " + event.x)
        val pointerIndex = event.findPointerIndex(mActivePointerId) // 得到按下點的index
        val x: Float = try {
            event.getX(pointerIndex)
        } catch (e: Exception) {
            return
        }
        if (Thumb.MIN == pressedThumb) {
            // screenToNormalized(x)-->得到規格化的0-1的值
            setNormalizedMinValue(screenToNormalized(x, 0))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x, 1))
        }
    }

    private fun screenToNormalized(screenCoord: Float, position: Int): Double {
        val width = width
        return if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            0.0
        } else {
            isMin = false
            var currentWidth = screenCoord.toDouble()
            val rangeL: Float = normalizedToScreen(normalizedMinValue)
            val rangeR: Float = normalizedToScreen(normalizedMaxValue)
            val min =
                mMinShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width - thumbWidth * 2)
            minWidth = if (absoluteMaxValuePrim > 5 * 60 * 1000) { // 大於5分鐘的精確小數四位
                val df = DecimalFormat("0.0000")
                df.format(min).toDouble()
            } else {
                (min + 0.5).roundToInt().toDouble()
            }
            if (position == 0) {
                if (isInThumbRangeLeft(screenCoord, normalizedMinValue, 0.5)) {
                    return normalizedMinValue
                }
                val rightPosition: Float = if (getWidth() - rangeR >= 0) getWidth() - rangeR else 0F
                val leftLength: Double = getValueLength() - (rightPosition + minWidth)
                if (currentWidth > rangeL) {
                    currentWidth = rangeL + (currentWidth - rangeL)
                } else if (currentWidth <= rangeL) {
                    currentWidth = rangeL - (rangeL - currentWidth)
                }
                if (currentWidth > leftLength) {
                    isMin = true
                    currentWidth = leftLength
                }
                if (currentWidth < thumbWidth * 2 / 3) {
                    currentWidth = 0.0
                }
                val resultTime = (currentWidth - padding) / (width - 2 * thumbWidth)
                normalizedMinValueTime = 1.0.coerceAtMost(0.0.coerceAtLeast(resultTime))
                val result = (currentWidth - padding) / (width - 2 * padding)
                1.0.coerceAtMost(0.0.coerceAtLeast(result)) // 保證該該值為0-1之間，但是什麼時候這個判斷有用呢？
            } else {
                if (isInThumbRange(screenCoord, normalizedMaxValue, 0.5)) {
                    return normalizedMaxValue
                }
                val rightLength: Double = getValueLength() - (rangeL + minWidth)
                if (currentWidth > rangeR) {
                    currentWidth = rangeR + (currentWidth - rangeR)
                } else if (currentWidth <= rangeR) {
                    currentWidth = rangeR - (rangeR - currentWidth)
                }
                var paddingRight = getWidth() - currentWidth
                if (paddingRight > rightLength) {
                    isMin = true
                    currentWidth = getWidth() - rightLength
                    paddingRight = rightLength
                }
                if (paddingRight < thumbWidth * 2 / 3) {
                    currentWidth = getWidth().toDouble()
                    paddingRight = 0.0
                }
                var resultTime = (paddingRight - padding) / (width - 2 * thumbWidth)
                resultTime = 1 - resultTime
                normalizedMaxValueTime = 1.0.coerceAtMost(0.0.coerceAtLeast(resultTime))
                val result = (currentWidth - padding) / (width - 2 * padding)
                1.0.coerceAtMost(0.0.coerceAtLeast(result)) // 保證該該值為0-1之間，但是什麼時候這個判斷有用呢？
            }
        }
    }

    private fun getValueLength(): Int = width - 2 * thumbWidth

    /**
     * 計算位於哪個Thumb內
     *
     * @param touchX touchX
     * @return 被touch的是空還是最大值或最小值
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed: Boolean = isInThumbRange(touchX, normalizedMinValue, 2.0) // 觸摸點是否在最小值圖片範圍內
        val maxThumbPressed: Boolean = isInThumbRange(touchX, normalizedMaxValue, 2.0)
        if (minThumbPressed && maxThumbPressed) {
            // 如果兩個thumbs重疊在一起，無法判斷拖動哪個，做以下處理
            // 觸摸點在屏幕右側，則判斷為touch到了最小值thumb，反之判斷為touch到了最大值thumb
            result = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed)
            result = Thumb.MIN
        else if (maxThumbPressed)
            result = Thumb.MAX

        return result
    }

    private fun isInThumbRange(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {
        // 當前觸摸點X坐標-最小值圖片中心點在屏幕的X坐標之差<=最小點圖片的寬度的一般
        // 即判斷觸摸點是否在以最小值圖片中心為原點，寬度一半為半徑的圓內。
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth * scale
    }

    private fun isInThumbRangeLeft(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {
        // 當前觸摸點X坐標-最小值圖片中心點在屏幕的X坐標之差<=最小點圖片的寬度的一般
        // 即判斷觸摸點是否在以最小值圖片中心為原點，寬度一半為半徑的圓內。
        return abs(touchX - normalizedToScreen(normalizedThumbValue) - thumbWidth) <= thumbHalfWidth * scale
    }

    /**
     * 試圖告訴父view不要攔截子控件的drag
     */
    private fun attemptClaimDrag() {
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true)
    }

    private fun onStartTrackingTouch() { mIsDragging = true }

    private fun onStopTrackingTouch() { mIsDragging = false }

    fun setMinShootTime(min_cut_time: Long) { mMinShootTime = min_cut_time }

    private fun normalizedToScreen(normalizedCoord: Double): Float =
        (paddingLeft + normalizedCoord * (width - paddingLeft - paddingRight)).toFloat()

    private fun valueToNormalized(value: Long): Double {
        return if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) { 0.0 }
        else (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)
    }

    fun setStartEndTime(start: Long, end: Long) {
        mStartPosition = start / 1000
        mEndPosition = end / 1000
    }

    fun setSelectedMinValue(value: Long) {
        if (0.0 == (absoluteMaxValuePrim - absoluteMinValuePrim))
            setNormalizedMinValue(0.0)
        else
            setNormalizedMinValue(valueToNormalized(value))
    }

    fun setSelectedMaxValue(value: Long) {
        if (0.0 == (absoluteMaxValuePrim - absoluteMinValuePrim))
            setNormalizedMaxValue(1.0)
        else
            setNormalizedMaxValue(valueToNormalized(value))
    }

    private fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = 0.0.coerceAtLeast(1.0.coerceAtMost(value.coerceAtMost(normalizedMaxValue)))
        invalidate() // 重新繪製此view
    }

    private fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = 0.0.coerceAtLeast(1.0.coerceAtMost(value.coerceAtLeast(normalizedMinValue)))
        invalidate() // 重新繪製此view
    }

    fun getSelectedMinValue(): Long = normalizedToValue(normalizedMinValueTime)

    fun getSelectedMaxValue(): Long = normalizedToValue(normalizedMaxValueTime)

    private fun normalizedToValue(normalized: Double): Long =
        (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)).toLong()

    /**
     * 供外部activity調用，控制是都在拖動的時候打印log信息，默認是false不打印
     */
    fun isNotifyWhileDragging(): Boolean = notifyWhileDragging

    fun setNotifyWhileDragging(flag: Boolean) { notifyWhileDragging = flag }

    fun setTouchDown(touchDown: Boolean) { isTouchDown = touchDown }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        bundle.putDouble("MIN_TIME", normalizedMinValueTime)
        bundle.putDouble("MAX_TIME", normalizedMaxValueTime)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
        normalizedMinValueTime = bundle.getDouble("MIN_TIME")
        normalizedMaxValueTime = bundle.getDouble("MAX_TIME")
    }

    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(
            bar: RangeSeekBarView?,
            minValue: Long,
            maxValue: Long,
            action: Int,
            isMin: Boolean,
            pressedThumb: Thumb?
        )
    }

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener) {
        mRangeSeekBarChangeListener = listener
    }
}