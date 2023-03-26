package com.side.project.video.ui.activity

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.side.project.video.databinding.ActivityVideoEditBinding
import com.side.project.video.ui.activity.other.BaseActivity
import com.side.project.video.utils.Method
import com.side.project.video.utils.VideoItem
import com.side.project.video.utils.other.util.VideoTrimListener

class VideoEditActivity : BaseActivity() {
    companion object {
        const val Tag = "VideoEditFragment"
        const val Arg_Video_Item = "Arg_Video_Item"
    }

    private val binding: ActivityVideoEditBinding by lazy { ActivityVideoEditBinding.inflate(layoutInflater) }

    /**
     * 參數
     */
    private lateinit var videoItem: VideoItem

    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent?.extras?.let {
            videoItem = Method.fromJsonString<VideoItem>(it.getString(Arg_Video_Item) ?: "") ?: VideoItem()
            if (videoItem.id == -1L) mActivity.onBackPressed()
        }

        doInitialize()
    }

    override fun onPause() {
        super.onPause()
        binding.trimmerView.onVideoPause()
        binding.trimmerView.setRestoreState(true)
    }

    override fun onDestroy() {
        binding.trimmerView.onDestroy()
        super.onDestroy()
    }

    /**
     * 執行動作
     */
    private fun doInitialize() {
        binding.run {
            trimmerView.setOnTrimVideoListener(object : VideoTrimListener {
                override fun onStartTrim() {
                    buildDialog("開始剪輯")?.show()
                }

                override fun onFinishTrim(url: String?) {
                    if (mProgressDialog?.isShowing == true) mProgressDialog?.dismiss()
                    Toast.makeText(mActivity, "完成剪輯", Toast.LENGTH_SHORT).show()
                    mActivity.onBackPressed()
                }

                override fun onCancel() {
                    trimmerView.onDestroy()
                    mActivity.onBackPressed()
                }
            })
            trimmerView.initVideoByURI(Uri.parse(videoItem.uri))
        }
    }

    private fun buildDialog(msg: String): ProgressDialog? {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(mActivity, "", msg)
        }
        mProgressDialog!!.setMessage(msg)
        return mProgressDialog
    }
}