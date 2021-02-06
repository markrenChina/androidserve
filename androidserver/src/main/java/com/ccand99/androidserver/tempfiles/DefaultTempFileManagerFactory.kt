package com.ccand99.androidserver.tempfiles

import javax.inject.Inject

class DefaultTempFileManagerFactory @Inject constructor(): TempFileManagerFactory {
    override fun create(): TempFileManager = DefaultTempFileManager()
}