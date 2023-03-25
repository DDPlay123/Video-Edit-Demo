package com.side.project.video.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.ArrayList

/**
 * Video 格式
 * @param id Video ID
 * @param uri Video URI
 * @param duration Video 時間長度
 */
data class VideoItem(val id: Long = -1, val uri: String = "", val duration: String = "")

/**
 * Video 工具
 */
object VideoUtils {
    /**
     * 取得所有 Video
     * @param context Context
     * @return List<VideoItem>
     */
    fun getAllShownVideoRes(context: Context): List<VideoItem> {
        try {
            val uriExternal: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val listOfAllVideos = ArrayList<VideoItem>()
            val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION)
            val videoSortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            val cursor = context.contentResolver.query(uriExternal, projection, null, null, videoSortOrder)
            cursor?.use {
                val columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val columnIndexDuration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (cursor.moveToNext()) {
                    val videoId = cursor.getLong(columnIndexID)
                    val uriVideo = Uri.withAppendedPath(uriExternal, "$videoId")
                    val duration = cursor.getLong(columnIndexDuration).takeIf { it > 0 }
                        ?: runBlocking { getVideoDurationAsync(context, uriVideo) }

                    listOfAllVideos.add(VideoItem(id = videoId, uri = uriVideo.toString(), duration = duration.toString()))
                }
                cursor.close()
            }
            return listOfAllVideos
        } catch (e: Exception) {
            e.printStackTrace()
            Method.logE("Get All Video Error:", "${e.message}")
            return ArrayList()
        }
    }

    /**
     * 取得指定 Video
     * @param context Context
     * @return VideoItem
     */
    fun getSpecifyVideoRes(context: Context, fileName: String): VideoItem {
        var mediaData = VideoItem()
        try {
            val uriExternal: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION)
            val selection = "${MediaStore.Video.Media.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf(fileName)

            val cursor = context.contentResolver.query(uriExternal, projection, selection, selectionArgs, null)
            cursor?.use {
                val columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val columnIndexDuration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (cursor.moveToNext()) {
                    val videoId = cursor.getLong(columnIndexID)
                    val uriVideo = Uri.withAppendedPath(uriExternal, "$videoId")
                    val duration = cursor.getLong(columnIndexDuration).takeIf { it > 0 }
                        ?: runBlocking { getVideoDurationAsync(context, uriVideo) }

                    mediaData = VideoItem(id = videoId, uri = uriVideo.toString(), duration = duration.toString())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Method.logE("Get Specify Video Error:", "${e.message}")
        }
        return mediaData
    }

    /**
     * 取得 Video 時間長度
     * @param context Context
     * @param uri Uri
     * @return Long?
     */
    suspend fun getVideoDurationAsync(context: Context, uri: Uri): Long? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(getPath(context, uri))
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationString?.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 取得 Video 路徑
     * @param context Context
     * @param uri Uri
     * @return String?
     */
    fun getPath(context: Context, uri: Uri): String? {
        // check here to KITKAT or new version
        //val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (/*isKitKat && */DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true))
                    return (Environment.getExternalStorageDirectory().toString() + "/" + split[1])
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is <span id="IL_AD2" class="IL_AD">useful</span> for MediaStore Uris, and other file-based
     * ContentProviders.
     *
     * @param context
     * The context.
     * @param uri
     * The Uri to query.
     * @param selection
     * (Optional) Filter used in the query.
     * @param selectionArgs
     * (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?,
        selection: String?, selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndex(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean =
        "com.android.externalstorage.documents" == uri.authority

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean =
        "com.android.providers.downloads.documents" == uri.authority

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean =
        "com.android.providers.media.documents" == uri.authority

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean =
        "com.google.android.apps.photos.content" == uri.authority
}