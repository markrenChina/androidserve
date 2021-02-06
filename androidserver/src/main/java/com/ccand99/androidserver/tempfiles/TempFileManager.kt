package com.ccand99.androidserver.tempfiles

/**
 * Temp file managers are created 1-to-1 with incoming requests, to create and cleanup temporary files created as a result of handling the request.
 * 临时文件管理器会与传入的请求1对1地创建，以创建和清理处理请求后创建的临时文件。
 */
public interface TempFileManager {

    fun clear()

    @Throws(Exception::class)
    fun createTempFile(filename_hint: String?): TempFile
}