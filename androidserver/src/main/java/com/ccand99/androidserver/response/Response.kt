package com.ccand99.androidserver.response

import com.ccand99.androidserver.ktfunction.trueToDo
import com.ccand99.androidserver.request.Method
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates


/**
 * HTTP response. Return one of these from serve().
 * @status HTTP status code after processing, e.g. "200 OK", Status.OK
 * @mimeType MIME type of content, e.g. "text/html"
 * @_data Data of the response, may be null.
 * @author markrenChina
 */
class Response(
    private val status: Status,
    private val mimeType: String,
    private var _data: InputStream?,
    private var totalBytes: Long
): Closeable {

    private var data: InputStream?= _data ?: ByteArrayInputStream(ByteArray(0))
    private val contentLength = _data?.let{ totalBytes } ?: 0L
    //Use chunkedTransfer
    //使用分块传输
    private val chunkedTransfer = contentLength < 0
    private val keepAlive = true
    //The request method that spawned this response.
    //产生此响应的请求方法。
    private lateinit var requestMethod: Method

    //todo
    /*
    private List<String> cookieHeaders;

    private GzipUsage gzipUsage = GzipUsage.DEFAULT;

    private static enum GzipUsage {
        DEFAULT,
        ALWAYS,
        NEVER;
    }
     */


    /**
     * Headers for the HTTP response. Use addHeader() to add lines. the
     * lowercase map is automatically kept up to date.
     */
    @SuppressWarnings("serial")
    private var header = object :HashMap<String?, String?>(){
        override fun put(key: String?, value: String?): String? {
            lowerCaseHeader[key?.toLowerCase(Locale.ROOT) ?: key] = value
            return super.put(key, value)
        }
    }

    //copy of the header map with all the keys lowercase for faster searching.
    //为了更快地搜索，所有键都小写的头图副本。
    private val lowerCaseHeader = HashMap<String?, String?>()

    /*private val header: MutableMap<String?, String?>? = object : HashMap<String?, String?>() {
        override fun put(key: String?, value: String?): String? {
            lowerCaseHeader.put(key?.toLowerCase() ?: key, value)
            return super.put(key, value)
        }
    }*/

    //Adds given line to the header.
    public fun addHeader(name: String?, value: String?) {
        header[name] = value
    }

    //Indicate to close the connection after the Response has been sent.
    //Params: close – true to hint connection closing, false to let connection be closed by client.
    public fun closeConnection(close: Boolean){
        (close) trueToDo {header["connection"]="close"} ?: header.remove("connection")
    }

    //Returns:
    //true if connection is to be closed after this Response has been sent.
    public fun isCloseConnection(): Boolean = "close" == header["connection"]


    override fun close() {
        data?.close()
    }

}