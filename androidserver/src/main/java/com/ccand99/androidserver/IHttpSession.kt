package com.ccand99.androidserver

import com.ccand99.androidserver.content.CookieHandler
import com.ccand99.androidserver.request.Method
import java.io.IOException
import java.io.InputStream

/**
 * Handles one session, i.e. parses the HTTP request and returns the response
 * 处理一个会话，解析HTTP请求并返回响应
 * @author markrenChina
 */
interface IHttpSession {

    @Throws(Exception::class)
    fun execute()

    var cookies: CookieHandler?

    var headers: Map<String,String>?

    val inputStream: InputStream

    var method : Method?

    var parameters: Map<String,List<String>>?

    var queryParameterString: String?

    //Returns: the path part of the URL.
    var uri: String?

    //Get the remote ip address of the requester.
    //Returns: the IP address.
    var remoteIpAddress: String?

    //Get the remote hostname of the requester.
    //Returns: the hostname.
    var remoteHostName: String?

    @Throws(IOException::class,ResponseException::class)
    fun parseBody(files : Map<String, String>)
}