package com.ccand99.androidserver.tempfiles

import java.io.OutputStream

/**
 * Temp files are responsible for managing the actual temporary storage and cleaning themselves up when no longer needed.
 * 临时文件负责管理实际的临时存储，并在不再需要时自行清理。
 */
public interface ITempFile {
    fun getName(): String

    @Throws(Exception::class)
    fun delete()

    //public fun getName(): String

    @Throws(Exception::class)
    fun open(): OutputStream
}