package com.ccand99.androidserver.tempfiles

import com.ccand99.androidserver.util.IFactory
import javax.inject.Inject

class DefaultTempFileManagerFactory @Inject constructor() : IFactory<ITempFileManager> {
    override fun create(): ITempFileManager = DefaultITempFileManager()
}