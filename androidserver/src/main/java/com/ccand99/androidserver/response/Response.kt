package com.ccand99.androidserver.response

import com.ccand99.androidserver.content.ContentType
import com.ccand99.androidserver.ktfunction.isTrue
import com.ccand99.androidserver.ktfunction.safeClose
import com.ccand99.androidserver.ktfunction.trueToDo
import com.ccand99.androidserver.request.Method
import com.ccand99.androidserver.util.MIME_HTML
import java.io.*
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.collections.ArrayList


/**
 * HTTP response. Return one of these from serve().
 * @status HTTP status code after processing, e.g. "200 OK", Status.OK
 * @mimeType MIME type of content, e.g. "text/html"
 * @_data Data of the response, may be null.
 * @author markrenChina
 */
open class Response(
    private val status: Status,
    private val mimeType: String?,
    private var _data: InputStream?,
    private var totalBytes: Long
) : Closeable {

    companion object {
        fun newChunkedResponse(status: Status, mimeType: String?, data: InputStream?) =
            Response(status, mimeType, data, -1)

        fun newFixedLengthResponse(status: Status, mimeType: String?, data: ByteArray? = null) =
            newFixedLengthResponse(
                status,
                mimeType,
                ByteArrayInputStream(data ?: ByteArray(0)),
                data?.size?.toLong() ?: 0L
            )

        fun newFixedLengthResponse(
            status: Status,
            mimeType: String?,
            data: InputStream?,
            totalBytes: Long
        ) = Response(status, mimeType, data, totalBytes)

        fun newFixedLengthResponse(status: Status, mimeType: String?, txt: String?): Response =
            txt?.let {
                val contentType = ContentType(mimeType)
                val bytes = txt.toByteArray(Charset.forName(contentType.encoding))
                newFixedLengthResponse(
                    status,
                    contentType.contentTypeHeader,
                    ByteArrayInputStream(bytes),
                    bytes.size.toLong()
                )
            } ?: run {
                newFixedLengthResponse(status, mimeType)
            }

        fun newFixedLengthResponse(msg: String) =
            newFixedLengthResponse(ResponseStatus.OK, MIME_HTML, msg)
    }

    private var data: InputStream? = _data ?: ByteArrayInputStream(ByteArray(0))
    private val contentLength = _data?.let { totalBytes } ?: 0L

    //Use chunkedTransfer
    //使用分块传输
    private var chunkedTransfer = contentLength < 0
    val keepAlive = true

    //The request method that spawned this response.
    //产生此响应的请求方法。
    var requestMethod: Method? = null

    val cookieHeaders: ArrayList<String> = ArrayList(10)

    private var gzipUsage: GzipUsage = GzipUsage.DEFAULT

    fun setUseGzip(useGzip: Boolean) {
        gzipUsage = useGzip isTrue GzipUsage.ALWAYS ?: GzipUsage.NEVER
    }

    private enum class GzipUsage {
        DEFAULT,
        ALWAYS,
        NEVER;
    }


    /**
     * Headers for the HTTP response. Use addHeader() to add lines. the
     * lowercase map is automatically kept up to date.
     */
    @SuppressWarnings("serial")
    private var header = object : HashMap<String?, String?>() {
        override fun put(key: String?, value: String?): String? {
            lowerCaseHeader[key?.toLowerCase(Locale.ROOT) ?: key] = value
            return super.put(key, value)
        }
    }

    //copy of the header map with all the keys lowercase for faster searching.
    //标题映射的副本，所有键都小写，以加快搜索速度
    private val lowerCaseHeader = object : HashMap<String?, String?>() {
        override fun get(key: String?): String? {
            return key?.let { super.get(it.toLowerCase(Locale.ROOT)) }

        }
    }

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
    public fun closeConnection(close: Boolean) {
        (close) trueToDo { header["connection"] = "close" } ?: header.remove("connection")
    }

    //Returns:
    //true if connection is to be closed after this Response has been sent.
    public fun isCloseConnection(): Boolean = "close" == header["connection"]


    /**
     * Sends given response to the socket.
     */
    fun send(outputStream: OutputStream) {
        val gmtFormat = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.ROOT)
        gmtFormat.timeZone = TimeZone.getTimeZone("GMT")
        outputStream.use { outputStream ->
            val pw = PrintWriter(
                BufferedWriter(
                    OutputStreamWriter(
                        outputStream,
                        ContentType(mimeType).encoding
                    )
                ), false
            )
            pw.append("HTTP/1.1 ").append(status.description).append(" \r\n")
            mimeType?.let { pw.printHeader("Content-Type", this.mimeType) }
            lowerCaseHeader["data"] ?: pw.printHeader("Date", gmtFormat.format(Date()))
            header.forEach { entry ->
                entry.key?.let { key ->
                    entry.value?.let { value ->
                        pw.printHeader(key, value)
                    }
                }
            }
            cookieHeaders.forEach { cookie ->
                pw.printHeader("Set-Cookie", cookie)
            }
            lowerCaseHeader["connection"] ?: pw.printHeader(
                "Connection",
                keepAlive isTrue "keep-alive" ?: "close"
            )
            lowerCaseHeader["content-length"]?.let { setUseGzip(false) }
            if (useGzipWhenAccepted()) {
                pw.printHeader("Content-Encoding", "gzip")
                chunkedTransfer = true
            }
            var pending = data?.let { contentLength } ?: 0L
            if (requestMethod != Method.HEAD && chunkedTransfer) {
                pw.printHeader("Transfer-Encoding", "chunked")
            } else if (!useGzipWhenAccepted()) {
                pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, pending)
            }
            pw.append("\r\n")
            pw.flush()
            sendBodyWithCorrectTransferAndEncoding(outputStream, pending)
            outputStream.flush()
            data.safeClose()
        }
    }

    private fun PrintWriter.printHeader(key: String, value: String) {
        this.append(key).append(": ").append(value).append("\r\n")
    }

    protected fun sendContentLengthHeaderIfNotAlreadyPresent(
        pw: PrintWriter,
        defaultSize: Long
    ): Long {
        val contentLengthStr = lowerCaseHeader["content-length"]
        var size = defaultSize
        contentLengthStr?.let {
            size = it.toLong()
        } ?: pw.printHeader("Content-Length", size.toString())
        return size
    }

    //使用正确的传输和编码发送正文
    private fun sendBodyWithCorrectTransferAndEncoding(outputStream: OutputStream, pending: Long) {
        if (requestMethod != Method.HEAD && chunkedTransfer) {
            val chuckedOutputStream = ChuckedOutputStream(outputStream)
            sendBodyWithCorrectEncoding(chuckedOutputStream, -1L)
            try {
                chuckedOutputStream.finish()
            } catch (e: Exception) {
                data?.close()
            }
        } else {
            sendBodyWithCorrectEncoding(outputStream, pending)
        }
    }

    private fun sendBodyWithCorrectEncoding(outputStream: OutputStream, pending: Long) {
        if (useGzipWhenAccepted()) {
            try {
                GZIPOutputStream(outputStream).use { gzipOutputStream ->
                    sendBody(gzipOutputStream, -1L)
                    gzipOutputStream.finish()
                }
            } catch (e: Exception) {
                data?.close()
            }
        } else sendBody(outputStream, pending)
    }

    private fun sendBody(outputStream: OutputStream, pending: Long) {
        val buff = ByteArray(DEFAULT_BUFFER_SIZE)
        val sendEverything = (pending == -1L)
        var size = pending
        data?.use { inputStream ->
            val input = inputStream.buffered()
            /*val output = outputStream.buffered()
            input.copyTo(output)*/
            while (size > 0 || sendEverything) {
                val bytesToRead = if (sendEverything) DEFAULT_BUFFER_SIZE else size.coerceAtMost(
                    DEFAULT_BUFFER_SIZE.toLong()
                )
                val read = inputStream.read(buff, 0, bytesToRead.toInt())
                if (read <= 0) {
                    break
                }
                try {
                    outputStream.write(buff, 0, read)
                } catch (e: java.lang.Exception) {
                    data?.close()
                }
                if (!sendEverything) {
                    size -= read
                }
            }
        }
    }

    override fun close() {
        data?.close()
    }

    // If a Gzip usage has been enforced, use it.
    // Else decide whether or not to use Gzip.
    private fun useGzipWhenAccepted() = if (gzipUsage == GzipUsage.DEFAULT) {
        mimeType != null && (mimeType.toLowerCase(Locale.ROOT)
            .contains("text/") || mimeType.toLowerCase(
            Locale.ROOT
        ).contains("/json"))
    } else gzipUsage == GzipUsage.ALWAYS

}