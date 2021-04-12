package com.ccand99.androidserve

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ccand99.androidserve", appContext.packageName)
    }

    @Test
    fun testio(){
        try {
            System.getProperty("java.io.tmpdir")?.let {
                println("test $it")
            } ?: println("test null")
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    @Test
    fun testInjectDefaultTempFileManager(){

    }
}