package com.ccand99.androidserver

import com.ccand99.androidserver.coroutines.IJobManager
import com.ccand99.androidserver.ktfunction.safeClose
import com.ccand99.androidserver.socket.BindDefaultServerSocketFactory
import com.ccand99.androidserver.tempfiles.BindDefaultTempFileManagerFactory
import com.ccand99.androidserver.tempfiles.ITempFileManager
import com.ccand99.androidserver.util.IFactory
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import javax.inject.Inject

/**
 * ContentType ct = new ContentType(session.getHeaders().get("content-type")).tryUTF8();
session.getHeaders().put("content-type", ct.getContentTypeHeader());
 */

//todo 后期修改主类继承为注入 ： class XXXX @Inject constructor()
abstract class MicroHttp(
    private val port: Int = 80,
    private val hostname: String? = null
) {

    //引入Hilt初始化
    /*@BindDefaultTempFileManager
    @Inject
    lateinit var tempFileManager: TempFileManager*/
    @BindDefaultTempFileManagerFactory
    @Inject
    lateinit var tempFileManagerFactory: IFactory<ITempFileManager>

    //before the fun start can change the ServerSocketFactory
    @BindDefaultServerSocketFactory
    @Inject
    @Volatile
    lateinit var serverSocketFactory: IFactory<ServerSocket>


    @Inject
    lateinit var jobManager: IJobManager

    private var myServerSocket: ServerSocket = ServerSocket()

    private var serverJob: Job? = null


    //@Inject lateinit var tempFileManagerFactory

    init {
        //setTempFileManagerFactory(DefaultTempFileManagerFactory())
        //setAsyncRunner(DefaultAsyncRunner())
    }

    @Throws(IOException::class)
    fun start(timeout: Int, daemon: Boolean) {
        println(myServerSocket)
        myServerSocket = serverSocketFactory.create()
        println(myServerSocket)
        myServerSocket.reuseAddress = true

        serverJob = createServerRunnable(timeout)
        /*myThread = Thread(serverRunnable)
        myThread?.isDaemon = daemon;
        myThread?.name = "MicroHttp Main Thread"
        myThread?.start()*/
    }

    suspend fun stop() {
        myServerSocket.safeClose()
        serverJob?.join()

    }

    /**
     * can be overwritten
     * @param timeout socket timeout to use
     * @return the server runnable
     */
    //protected fun createServerRunnable(timeout: Int) = ServerRunnable(this,timeout)
    //todo blockscope?
    protected fun createServerRunnable(timeout: Int) = CoroutineScope(Dispatchers.Default).launch {
        withContext(Dispatchers.IO) {
            myServerSocket.bind(hostname?.let { InetSocketAddress(it, port) }
                ?: InetSocketAddress(port))
        }
        do {
            //阻塞 死循环等待新连接，每一个连接都创建一个客户端，交给asyncRunner管理
            myServerSocket.accept().use { socket ->
                if (timeout > 0) {
                    socket.soTimeout = timeout
                }
                socket.getInputStream().use { inputStream ->
                    jobManager.exec(ClientJob(this@MicroHttp, inputStream, socket).clientJob)
                }
            }
        } while (!myServerSocket.isClosed)
    }


}