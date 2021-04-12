package com.ccand99.androidserver

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ccand99.androidserver.tempfiles.DefaultTempFileManager

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import javax.inject.Inject

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Inject
    lateinit var defaultTempFileManager: DefaultTempFileManager

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ccand99.androidserver.test", appContext.packageName)
    }

    @Test
    fun testInjectDefaultTempFileManager(){
        println(defaultTempFileManager.clear())

    }
}