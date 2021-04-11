package com.ccand99.androidserver.util

import android.util.Log
import com.ccand99.androidserver.MicroHttp
import kotlinx.coroutines.*

object ServerRunner {

    suspend fun executeInstance(server: MicroHttp) = coroutineScope {
        withContext(Dispatchers.Default) {
            Log.i(ServerRunner.javaClass.name, "executeInstance: start server")
            server.start(SOCKET_READ_TIMEOUT, false)
        }
        withContext(Dispatchers.IO) {
            Log.i(ServerRunner.javaClass.name, "executeInstance: stop server")
            server.stop()
        }
    }

    fun <T : MicroHttp> run(serverClass: Class<T>) {
        GlobalScope.launch {
            executeInstance(serverClass.newInstance())
        }
    }
}