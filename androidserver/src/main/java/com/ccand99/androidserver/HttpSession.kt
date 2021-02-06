package com.ccand99.androidserver

import android.util.Log
import com.ccand99.androidserver.content.ContentType
import com.ccand99.androidserver.content.CookieHandler
import com.ccand99.androidserver.ktfunction.decodePercent
import com.ccand99.androidserver.ktfunction.isTrue
import com.ccand99.androidserver.ktfunction.safeClose
import com.ccand99.androidserver.ktfunction.trueDoBack
import com.ccand99.androidserver.request.Method
import com.ccand99.androidserver.response.ResponseStatus
import com.ccand99.androidserver.tempfiles.TempFileManager
import java.io.*
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author markrenChina
 */
class HttpSession @Inject constructor(
    val tempFileManager: TempFileManager,
    private val _inputStream: InputStream,
    val outputStream: OutputStream,
    val inetAddress: InetAddress? = null,
    override var cookies: CookieHandler? = null,
    override var headers: Map<String, String>? = null,
    override var method: Method? = null,
    override var parameters: Map<String, List<String>>? = null,
    override var queryParameterString: String? = null,
    override var uri: String?,
    override var remoteIpAddress: String? = null,
    override var remoteHostName: String? = null
): IHttpSession{

    companion object{
        //请求缓存长度
        private const val REQUEST_BUFFER_LEN = 512
        //内存限制
        private const val MEMORY_STORE_LIMIT = 1024
        //缓存长度
        private const val BUFSIZE = 8192
        //最大头长度
        private const val MAX_HEADER_SIZE = 1024
        private const val CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)"
        private val CONTENT_DISPOSITION_PATTERN = Pattern.compile(
            CONTENT_DISPOSITION_REGEX,
            Pattern.CASE_INSENSITIVE
        )
        private const val CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]"
        private val CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(
            CONTENT_DISPOSITION_ATTRIBUTE_REGEX
        )
        private const val CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)"
        private val CONTENT_TYPE_PATTERN =
            Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE)

    }

    override val inputStream= BufferedInputStream(_inputStream, BUFSIZE)

    var protocolVersion: String?= null
    private var splitbyte = 0L
    private var rlen = 0L

    init {
        inetAddress?.let {
            remoteIpAddress = (it.isLoopbackAddress || it.isAnyLocalAddress) isTrue "127.0.0.1"
                ?: it.hostAddress
            remoteHostName = (it.isLoopbackAddress || it.isAnyLocalAddress) isTrue "localhost"
                ?: it.hostName
            headers = HashMap()
        }
    }

    //Decodes the sent headers and loads the data into Key/value pairs
    //解码发送的报头并将数据加载到键/值对中
    @Throws(ResponseException::class)
    private fun decodeHeader(
        bufferedReader: BufferedReader,
        pre: HashMap<String, String>,
        params: HashMap<String, List<String>>,
        headers: HashMap<String, String>
    ){
        try {
            val lines: List<String> = bufferedReader.readLines()
            Log.d("http head：", lines.toString())
            if (lines[0].isEmpty()){ return }
            val st = StringTokenizer(lines[0])
            if (!st.hasMoreTokens()){
                throw ResponseException(
                    ResponseStatus.BAD_REQUEST,
                    "BAD REQUEST: Syntax error. Usage: GET /example/file.html"
                )
            }
            pre["method"] = st.nextToken()
            if (!st.hasMoreTokens()){
                throw ResponseException(
                    ResponseStatus.BAD_REQUEST,
                    "BAD REQUEST: Syntax error. Usage: GET /example/file.html"
                )
            }
            var uri = st.nextToken()
            //解码URI中的参数
            val qmi = uri.indexOf('?')
            uri = (qmi >= 0) trueDoBack  {
                decodeParams(uri.substring(qmi + 1), params)
                decodePercent(uri.substring(0, qmi))
            }?: decodePercent(uri)
            pre["uri"] = uri
            //If there's another token, its protocol version
            protocolVersion = st.hasMoreTokens() trueDoBack {st.nextToken()}
                ?: run{
                    Log.i(
                        "HttpSession",
                        "no protocol version specified, strange. Assuming HTTP/1.1."
                    )
                    "HTTP/1.1"
                }
            val header = with(String){
                for (index in 1 until lines.size){
                    val pos = lines[index].indexOf(':')
                    if (pos >= 0 ) {
                        headers[lines[index].substring(0, pos).trim().toLowerCase(Locale.US)] = lines[index].substring(
                            pos + 1
                        ).trim()
                    }
                }
            }

        }catch (ioe: IOException){
            throw ResponseException(
                ResponseStatus.INTERNAL_ERROR,
                "SERVER INTERNAL ERROR: IOException: ${ioe.message}",
                ioe
            )
        }
    }

    //Decodes the Multipart Body data and put it into Key/Value pairs.
    //解码二进制类型（多包）body数据并将其放入键/值对中。
    @Throws(ResponseException::class)
    private fun decodeMultipartFormData(
        contentType: ContentType,
        fbuf: ByteBuffer,
        parms: HashMap<String, List<String>>,
        files: HashMap<String, String>
    ){
        var pcount = 0
        try {
            val boundaryIdxs = getBoundaryPositions(fbuf, contentType.boundary!!.toByteArray())
            if (boundaryIdxs.size < 2){
                throw ResponseException(
                    ResponseStatus.BAD_REQUEST,
                    "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings."
                )
            }

            val partHeaderBuff = ByteArray(MAX_HEADER_SIZE)
            for ((index,it) in boundaryIdxs.withIndex()) {
                fbuf.position(it)
                val len = (fbuf.remaining() < MAX_HEADER_SIZE) isTrue fbuf.remaining() ?: MAX_HEADER_SIZE
                fbuf.get(partHeaderBuff, 0, len)
                val bufferedRead = BufferedReader(
                    InputStreamReader(
                        ByteArrayInputStream(
                            partHeaderBuff,
                            0,
                            len
                        ),
                        Charset.forName(contentType.encoding)
                    ), len
                )
                var headersLiens = 0
                //First line is boundary string
                var mpline:String? = bufferedRead.readLine()
                headersLiens++
                if (mpline == null || !mpline.contains(contentType.boundary)){
                    throw ResponseException(
                        ResponseStatus.BAD_REQUEST,
                        "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary."
                    )
                }
                var partName: String?= null
                var fileName: String?= null
                var partContentType: String?= null
                //Parse the reset of the header lines
                mpline = bufferedRead.readLine()
                headersLiens++
                while (mpline != null && mpline.trim().isNotEmpty()){
                    var matcher = CONTENT_DISPOSITION_PATTERN.matcher(mpline)
                    if (matcher.matches()){
                        val attributeString = matcher.group(2)
                        matcher = CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString!!)
                        while (matcher.find()){
                            val key = matcher.group(1)
                            if ("name".equals(key, true)){
                                partName = matcher.group(2)
                            }else if ("filename".equals(key, true)){
                                fileName = matcher.group(2)
                                // add these two line to support multiple
                                // files uploaded using the same field Id
                                if (!fileName.isEmpty()){
                                    if (pcount > 0){
                                        partName += (pcount++).toString()
                                    }else{
                                        pcount++
                                    }
                                }
                            }
                        }
                    }
                    matcher = CONTENT_TYPE_PATTERN.matcher(mpline)
                    if (matcher.matches()){
                        partContentType = matcher.group(2)?.trim()
                    }
                    mpline = bufferedRead.readLine()
                    headersLiens++
                }
                var partHeaderLength = 0
                while (headersLiens -- > 0){
                    partHeaderLength = scipOverNewLine(partHeaderBuff,partHeaderLength)
                }
                //Read the part data
                if (partHeaderLength >= len - 4){
                    throw  ResponseException(ResponseStatus.INTERNAL_ERROR,
                        "Multipart header size exceeds MAX_HEADER_SIZE."
                    )
                }
                val partDataStart = it + partHeaderLength
                val partDataEnd = boundaryIdxs[index + 1] - 4

                fbuf.position(partDataStart)

                var values = parms[partName] as? ArrayList<String>
                if (values == null){
                    values = ArrayList<String>()
                    parms[partName!!] = values
                }

                if (partContentType == null){
                    // Read the part into a string
                    val dataBytes = ByteArray(partDataEnd - partDataStart)
                    fbuf.get(dataBytes)

                    values.add(String(dataBytes, charset(contentType.encoding)))
                }else{
                    //Read it into a file
                    val path = saveTmpFile(fbuf,partDataStart,partDataEnd-partDataStart,fileName)
                    if (!files.containsKey(partName)){
                        files[partName!!] = path
                    }else {
                        var count = 2
                        while (files.containsKey(partName + count)){
                            count ++
                        }
                        files[partName + count] = path
                    }
                    values.add(fileName?:"null")
                }
            }
        }catch (e: Exception){

        }
    }

    fun scipOverNewLine(partHeaderBuff: ByteArray, index: Int): Int{
        var varg = index
        while (partHeaderBuff[index] != '\n'.toByte()){
            varg++
        }
        return ++varg
    }

    //Decodes parameters in percent-encoded URI-format
    // ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" )
    // and adds them to given Map.
    //解码URL中query属性
    private fun decodeParams(parms: String?, p: HashMap<String, List<String>>){
        if (parms == null){
            queryParameterString = ""
            return
        }

        queryParameterString = parms
        val st = StringTokenizer(parms,"&")
        while (st.hasMoreTokens()){
            val e = st.nextToken()
            val sep = e.indexOf('=')
            var key : String?= null
            var value: String?= null

            if (sep >= 0){
                key = decodePercent(e.substring(0,sep))?.trim()
                value = decodePercent(e.substring(sep + 1))
            }else{
                key = decodePercent(e)?.trim()
                value = ""
            }

            var values = p[key] as? ArrayList<String>
            if (values == null){
                values = ArrayList()
                key?.let { p[it] = values }
            }
            value?.let { values.add(it)}
        }
    //todo 通过带改变HashMap参数值带回值，这样不好
    }

    override fun execute(){
        val r: Response?= null
    }

    //Find byte index separating header from body.
    //It must be the last byte of the first two sequential new lines.
    //查找将头部与正文分开的字节索引。它必须是连续的前两个新行的最后一个字节
    private fun findHeaderEnd(buf: ByteArray, rlen: Int){

    }

    //Find the byte positions where multipart boundaries start.
    //This reads a large block at a time and uses a temporary buffer to optimize (memory mapped) file access.
    //查找多部分边界开始的字节位置。它每次读取一个大的块，并使用一个临时缓冲区来优化(内存映射)文件访问。
    private fun getBoundaryPositions(b: ByteBuffer, boundary: ByteArray): IntArray{
        var res = IntArray(0)
        if (b.remaining() < boundary.size){
            return res
        }

        var searchWindowPos = 0
        val searchWindow = ByteArray(4 * 1024 + boundary.size)

        //第一次读取
        val firstFill = (b.remaining() < searchWindow.size) isTrue b.remaining() ?: searchWindow.size
        b.get(searchWindow, 0, firstFill)
        var newBytes = firstFill - boundary.size

        do {
            for ( j in 0 until newBytes){
                for ( i in boundary.indices){
                    if (searchWindow[i + j] != boundary[i]){
                        break
                    }
                    if (i == boundary.size - 1){
                        // Match found, add it to results
                        val newRes = IntArray(res.size + 1)
                        System.arraycopy(res, 0, newRes, 0, res.size)
                        newRes[res.size] = searchWindowPos + 1
                        res = newRes
                    }
                }
            }
            searchWindowPos += newBytes

            // Copy the end of the buffer to the start
            System.arraycopy(
                searchWindow, searchWindow.size - boundary.size,
                searchWindow, 0, boundary.size
            )

            //Refill search_window
            newBytes = searchWindow.size - boundary.size
            newBytes = (b.remaining() < newBytes) isTrue b.remaining() ?: newBytes
            b.get(searchWindow, boundary.size, newBytes)
        }while (newBytes > 0)
        return res
    }

    private fun getTmpBucket(): RandomAccessFile{
        return try {
            val tempFile = this.tempFileManager.createTempFile(null)
            RandomAccessFile(tempFile.name, "rw")
        }catch (e: Exception) {
            throw Error(e)
        }
    }

    //Deduce body length in bytes. Either from "content-length" header or read bytes.
    //推断体长度(以字节为单位)。要么从“content-length”头中读取，要么读取字节
    public fun getBodySize(): Long {
        return if (headers != null && headers!!.containsKey("content-length")) {
            headers!!["content-length"]!!.toLong()
        } else if (splitbyte < rlen) {
            rlen - splitbyte
        } else {
            0L
        }
    }

    @Throws(IOException::class, ResponseException::class)
    override fun parseBody(files: Map<String, String>) {
        var randomAccessFile: RandomAccessFile?= null

    }

    //Retrieves the content of a sent file and saves it to a temporary file.
    //The full path to the saved file is returned.
    //检索已发送文件的内容，并将其保存到临时文件中。将返回所保存文件的完整路径。
    private fun saveTmpFile(b: ByteBuffer,offset: Int,len: Int,filename_hint: String?):String{
        var path = ""
        if (len > 0){
            var fileOutputStream: FileOutputStream?= null
            try {
                val tempFile = tempFileManager.createTempFile(filename_hint)
                val src = b.duplicate()
                fileOutputStream = FileOutputStream(tempFile.name)
                val dest = fileOutputStream.channel
                src.position(offset).limit(offset + len)
                dest.write(src.slice())
                path = tempFile.name
            }catch (e: Exception){
                throw Error(e)
            }finally {
                fileOutputStream.safeClose()
            }
        }
        return path
    }
}