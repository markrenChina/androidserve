package com.ccand99.androidserver.tempfiles

import com.ccand99.androidserver.ktfunction.safeClose
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Default strategy for creating and cleaning up temporary files.
 * 创建和清理临时文件的默认策略。
 * By default, files are created by File.createTempFile() in the directory specified.
 * 默认情况下，文件是由指定目录中的File.createTempFile()创建的。
 */
//@JvmStatic
class DefaultITempFile @Inject constructor(tempFile: File) : ITempFile {
    private val file: File = File.createTempFile("MicroHttp-", "", tempFile)
    private val fStream = FileOutputStream(file)

    @Throws(Exception::class)
    override fun delete() {
        fStream.safeClose()
        if (!this.file.delete()) {
            throw Exception("could not delete temporary file: " + file.absolutePath)
        }
    }

    override fun getName(): String = file.absolutePath

    @Throws(Exception::class)
    override fun open(): OutputStream = fStream
}