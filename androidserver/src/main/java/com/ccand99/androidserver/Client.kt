package com.ccand99.androidserver

import android.util.Log
import com.ccand99.androidserver.ktfunction.safeClose
import com.ccand99.androidserver.tempfiles.BindDefaultTempFileManager
import com.ccand99.androidserver.tempfiles.TempFileManager
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject

/**
 * The class that will be used for every client connection
 *
 * @author markrenChina
 */
public class Client @Inject constructor(
    private val microHttp: MicroHttp,
    private val inputStream: InputStream,
    private val acceptSocket: Socket
    ){

    @BindDefaultTempFileManager
    @Inject
    private lateinit var tempFileManager : TempFileManager

    //todo 在try里面是否需要注入要考虑一下
    @Inject
    private lateinit var session : HttpSession

    fun close(){
        inputStream.safeClose()
        acceptSocket.safeClose()
    }

    fun run(){
        var outputStream : OutputStream?= null
        try {
            outputStream = acceptSocket.getOutputStream()
            while (!acceptSocket.isClosed){
                session.execute()
            }
        }catch (e : Exception){
            if (!(e is SocketException && "MicroHttp Shutdown" == e.message)){
                Log.w("Client", "Communication with the client broken, or an bug in the handler code",e)
            }
        }finally {
            outputStream.safeClose()
            inputStream.safeClose()
            acceptSocket.safeClose()
            microHttp.asyncRuner.closed(this)
        }
    }
}