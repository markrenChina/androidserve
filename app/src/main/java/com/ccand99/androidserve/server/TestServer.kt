package com.ccand99.androidserve.server

import android.util.Log
import com.ccand99.androidserver.IHttpSession
import com.ccand99.androidserver.MicroHttp
import com.ccand99.androidserver.response.Response
import com.ccand99.androidserver.response.Response.Companion.newFixedLengthResponse

class TestServer(port: Int = 80, hostname: String? = null) : MicroHttp(port, null) {

    fun serve(session: IHttpSession?): Response {
        return newFixedLengthResponse("测试成功")
    }

    init {
        Log.i("TestServer", "init")
    }
}