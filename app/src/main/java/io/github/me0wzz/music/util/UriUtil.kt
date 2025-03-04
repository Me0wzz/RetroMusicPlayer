package io.github.me0wzz.music.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi

object UriUtil {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getUriFromPath(context: Context, path: String): Uri {
        val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val proj = arrayOf(MediaStore.Files.FileColumns._ID)
        context.contentResolver.query(
            uri, proj, MediaStore.Files.FileColumns.DATA + "=?", arrayOf(path), null
        )?.use { cursor ->
            if (cursor.count != 0) {
                cursor.moveToFirst()
                return ContentUris.withAppendedId(uri, cursor.getLong(0))
            }
        }
        return Uri.EMPTY
    }
}