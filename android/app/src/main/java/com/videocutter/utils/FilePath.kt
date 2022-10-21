package com.videocutter.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.lang.Exception

object FilePath {

    const val TYPE_VIDEO = "video"
    const val TYPE_IMAGE = "image"

    /**
     * Method for return file path of Gallery image
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */

    @JvmStatic
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri,contentType : String = ""): String? {
        //check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4)
                }

                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads"
                )

                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))
                        val path = getDataColumn(context, contentUri, null, null)
                        if (path != null) {
                            return path
                        }
                    } catch (e: Exception) {
                    }
                }

                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                var fileName: String? = getDataColumn(context, uri,null,null)
                if(fileName.isNullOrBlank() && contentType == TYPE_VIDEO){
                    fileName = "${"FILE_"}${System.currentTimeMillis()}.mp4"
                }
                val cacheDir: File = getDocumentCacheDir(context)
                val file: File = generateFileName(fileName, cacheDir)
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.absolutePath
                    saveFileFromUri(context, uri, destinationPath)
                }

                return destinationPath

               /* val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )

                return getDataColumn(context, contentUri, null, null)*/
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
            // MediaProvider
            //DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    private fun generateFileName(fileName: String?, cacheDir: File): File {
        Log.d("FileName",fileName)
        return File(cacheDir, fileName)
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
         return "com.google.android.apps.photos.contentprovider" == uri.authority
    }


    fun getDocumentCacheDir(context: Context): File {
        val dir =  File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

  /*  private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            `is`?.read(buf)
            do {
                bos.write(buf)
            } while (`is`?.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

    private fun saveFileFromUri(context: Context, uri: Uri, dstPath: String): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            outputStream = FileOutputStream(dstPath)
            val buff = ByteArray(100 * 1024)
            var len: Int
            while (inputStream!!.read(buff).also { len = it } != -1) {
                outputStream.write(buff, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()

        }
        return true
    }

/*
    fun getFileName(@NonNull context: Context?, uri: Uri): String? {
        val mimeType = context!!.contentResolver.getType(uri)
        var filename: String? = null
        if (mimeType == null && context != null) {
            val path = getPath(context, uri)
            if (path == null) {
                filename = getName(uri.toString())
            } else {
                val file = File(path)
                filename = file.name
            }
        } else {
            val returnCursor = context.contentResolver.query(
                uri, null,
                null, null, null
            )
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }
        return filename
    }
*/


}
