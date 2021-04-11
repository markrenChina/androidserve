package com.ccand99.androidserver

import java.io.InputStream
import java.net.Socket
import javax.inject.Inject

/**
 * The class that will be used for every client connection
 *
 * @author markrenChina
 */
@Deprecated(message = "use createClientJob")
public class Client @Inject constructor(
    private val microHttp: MicroHttp,
    private val inputStream: InputStream,
    private val acceptSocket: Socket
) {

    /*@BindDefaultTempFileManager
    @Inject
    lateinit var ITempFileManager : ITempFileManager

    //todo 在try里面是否需要注入要考虑一下
    @Inject
    lateinit var session : HttpSession

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
            //microHttp.asyncRuner.closed(this)
        }
    }*/
}