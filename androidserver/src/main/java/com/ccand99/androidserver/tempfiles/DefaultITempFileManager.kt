package com.ccand99.androidserver.tempfiles

import android.util.Log
import java.io.File
import javax.inject.Inject

class DefaultITempFileManager @Inject constructor() : ITempFileManager {

    //目录： data/user/0/com.ccand99.androidserve/cache
    private val tmpdir: File = File(
        System.getProperty("java.io.tmpdir") ?: "/storage/emulated/0/androidserve/cache"
    )
    private val tempFiles = ArrayList<ITempFile>()

    init {
        Log.d("DefaultTempFileManager", "inject success")
        if (!tmpdir.exists()) {
            try {
                tmpdir.mkdirs()
            } catch (e: Exception) {
                Log.e("DefaultTempFileManager", "could not create dir", e)
            }
        }
    }

    override fun clear() {
        tempFiles.forEach {
            try {
                it.delete()
            } catch (ignored: Exception) {
                Log.w("DefaultTempFileManager", "could not delete file ", ignored)
            }
        }
    }


    override fun createTempFile(filename_hint: String?): ITempFile {
        val tempFile = DefaultITempFile(tmpdir)
        tempFiles.add(tempFile)
        return tempFile
    }

}