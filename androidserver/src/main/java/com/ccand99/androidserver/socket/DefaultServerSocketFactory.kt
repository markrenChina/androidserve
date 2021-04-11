package com.ccand99.androidserver.socket

import com.ccand99.androidserver.util.IFactory
import java.net.ServerSocket

class DefaultServerSocketFactory : IFactory<ServerSocket> {

    override fun create(): ServerSocket = ServerSocket();

}