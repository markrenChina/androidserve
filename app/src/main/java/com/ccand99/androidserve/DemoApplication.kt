package com.ccand99.androidserve

import android.app.Application
import com.ccand99.androidserve.server.TestServer
import com.ccand99.androidserver.util.ServerRunner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TestServer(8888)
        ServerRunner.run(TestServer::class.java)
    }
}