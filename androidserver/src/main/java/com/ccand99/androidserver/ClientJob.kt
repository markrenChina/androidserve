package com.ccand99.androidserver

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.Socket
import java.net.SocketException


class ClientJob(
    private val microHttp: MicroHttp,
    private val inputStream: InputStream,
    private val socket: Socket
) {

    val clientJob = CoroutineScope(Dispatchers.IO).launch {
        try {
            val tempFileManager = microHttp.tempFileManagerFactory.create()
            socket.use { socket ->
                socket.getOutputStream().use { outputStream ->
                    inputStream.use { inputStream ->
                        val session = HttpSession(
                            microHttp,
                            tempFileManager,
                            inputStream,
                            outputStream,
                            socket.inetAddress
                        )
                        while (!socket.isClosed) {
                            session.execute()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            if (!(e is SocketException && "MicroHttp Shutdown" == e.message)) {
                Log.w(
                    "Client",
                    "Communication with the client broken, or an bug in the handler code",
                    e
                )
            }
        } finally {
            close()
        }
    }

    private fun close() {
        microHttp.jobManager.closed(clientJob)
    }

}




