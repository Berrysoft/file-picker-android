package com.plugin.berrysoft.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResult
import app.tauri.annotation.ActivityCallback
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSArray
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

@TauriPlugin
class PickerPlugin(private val activity: Activity): Plugin(activity) {
    @Command
    fun pickFiles(invoke: Invoke) {
        val value = invoke.getArray("extensions")?.toList<String>()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.putExtra("multi-pick", true)
        this.startActivityForResult(invoke, intent, "onPickedFiles")
    }

    @ActivityCallback
    fun onPickedFiles(invoke: Invoke, result: ActivityResult) {
        val intent = result.getData();
        val paths: MutableList<String> = ArrayList()
        if (result.resultCode == Activity.RESULT_OK && intent != null) {
            val clipData = intent.getClipData()
            if (clipData != null) {
                val count = clipData.getItemCount()
                for (i in 0 until count) {
                    val currentUri = clipData.getItemAt(i).getUri()
                    val currentPath = getCopyFilePath(currentUri)
                    if (currentPath != null) {
                        paths.add(currentPath)
                    }
                }
            }
        }
        val ret = JSObject()
        ret.put("paths", JSArray(paths))
        invoke.resolve(ret)
    }

    fun getCopyFilePath(uri: Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        val file = File(activity.filesDir, name)
        try {
            val inputStream = activity.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024 * 1024
            val bufferSize = min(inputStream!!.available(), maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            return null
        } finally {
            cursor.close()
        }
        return file.path
    }
}
