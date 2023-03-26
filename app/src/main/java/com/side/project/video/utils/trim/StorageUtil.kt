package com.side.project.video.utils.trim

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.side.project.video.BuildConfig
import java.io.File
import java.io.IOException
import java.util.*

object StorageUtil {
    private const val TAG = "StorageUtil"
    private const val APP_DATA_PATH = "/Android/data/" + BuildConfig.APPLICATION_ID
    private var sDataDir: String? = null
    private var sCacheDir: String? = null

    //判断文件目录是否存在
    val appDataDir: String?
        get() {
            if (TextUtils.isEmpty(sDataDir)) {
                try {
                    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                        sDataDir = Environment.getExternalStorageDirectory().path + APP_DATA_PATH
                        if (TextUtils.isEmpty(sDataDir)) {
                            sDataDir = BaseUtils.getContext().filesDir.absolutePath
                        }
                    } else {
                        sDataDir = BaseUtils.getContext().filesDir.absolutePath
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    sDataDir = BaseUtils.getContext().filesDir.absolutePath
                }
                val file = File(sDataDir.toString())
                if (!file.exists()) { // 判斷文件目錄是否存在
                    file.mkdirs()
                }
            }
            return sDataDir
        }

    val cacheDir: String
        get() {
            if (TextUtils.isEmpty(sCacheDir)) {
                var file: File? = null
                val context = BaseUtils.getContext()
                try {
                    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                        file = context.externalCacheDir
                        if (file == null || !file.exists()) {
                            file = getExternalCacheDirManual(context)
                        }
                    }
                    if (file == null) {
                        file = context.cacheDir
                        if (file == null || !file.exists()) {
                            file = getCacheDirManual(context)
                        }
                    }
                    Log.w(TAG, "cache dir = " + file.absolutePath)
                    sCacheDir = file.absolutePath
                } catch (ignored: Throwable) {
                }
            }
            return sCacheDir.toString()
        }

    private fun getExternalCacheDirManual(context: Context): File? {
        val dataDir = File(File(Environment.getExternalStorageDirectory(), "Android"), "data")
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) { //
                Log.w(TAG, "Unable to create external cache directory")
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                Log.i(TAG, "Can't create \".nomedia\" file in application external cache directory")
            }
        }
        return appCacheDir
    }

    @SuppressLint("SdCardPath")
    private fun getCacheDirManual(context: Context): File {
        val cacheDirPath = "/data/data/" + context.packageName + "/cache"
        return File(cacheDirPath)
    }

    // 刪除文件夾下所有文件和文件夾
    fun delFiles(path: String?): Boolean {
        val cacheFile = File(path.toString())
        if (!cacheFile.exists()) return false
        val files = cacheFile.listFiles() ?: return false
        for (i in files.indices) {
            // 是文件則直接刪除
            if (files[i].exists() && files[i].isFile) {
                files[i].delete()
            } else if (files[i].exists() && files[i].isDirectory) {
                // 遞歸刪除文件
                delFiles(files[i].absolutePath)
                // 刪除完目錄下面的所有文件後再刪除該文件夾
                files[i].delete()
            }
        }
        return true
    }

    fun sizeOfDirectory(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles() ?: return 0
            for (i in fileList.indices) {
                // Recursive call if it's a directory
                result += if (fileList[i].isDirectory) {
                    sizeOfDirectory(fileList[i])
                } else {
                    // Sum the file size in bytes
                    fileList[i].length()
                }
            }
            return result // return the file size
        }
        return 0
    }

    /**
     * @param length 長度 byte為單位
     * 將文件大小轉換為KB,MB格式
     */
    fun getFileSize(length: Long): String {
        val mb = 1024 * 1024
        if (length < mb) {
            val resultKB = length * 1.0 / 1024
            return String.format(Locale.getDefault(), "%.1f", resultKB) + "Kb"
        }
        val resultMB = length * 1.0 / mb
        return String.format(Locale.getDefault(), "%.1f", resultMB) + "Mb"
    }

    fun isFileExist(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) return false
        val file = File(path.toString())
        return file.exists()
    }

    /**
     * @param path 路徑
     * @return 是否删除成功
     */
    fun deleteFile(path: String?): Boolean =
        if (TextUtils.isEmpty(path)) true else deleteFile(File(path.toString()))

    /**
     * @return 是否删除成功
     */
    fun deleteFile(file: File?): Boolean {
        if (file == null || !file.exists()) return true
        if (file.isFile)
            return file.delete()
        if (!file.isDirectory)
            return false

        for (f in file.listFiles()!!) {
            if (f.isFile)
                f.delete()
            else if (f.isDirectory)
                deleteFile(f)
        }
        return file.delete()
    }
}