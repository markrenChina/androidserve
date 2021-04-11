package com.ccand99.androidserver.content

import android.util.Log
import com.ccand99.androidserver.ktfunction.trueDoBack
import java.util.regex.Pattern

public class ContentType(
    val contentTypeHeader: String?

) {
    companion object {
        //private const val ASCII_ENCODING = "US-ASCII"
        private const val UTF_8_ENCODING = "UTF-8"
        private const val MULTIPART_FORM_DATA_HEADER = "multipart/form-data"
        private const val CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)"
        private val MIME_PATTERN = Pattern.compile(CONTENT_REGEX, Pattern.CASE_INSENSITIVE)
        private const val CHARSET_REGEX =
            "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?"
        private val CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, Pattern.CASE_INSENSITIVE)
        private const val BOUNDARY_REGEX =
            "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?"
        private val BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE)
    }

    //主体的媒体类型
    val contentType = contentTypeHeader?.let {
        getDetailFromContentHeader(contentTypeHeader, MIME_PATTERN, "", 1)
    } ?: ""

    //编码方式
    val encoding = contentTypeHeader?.let {
        getDetailFromContentHeader(contentTypeHeader, CHARSET_PATTERN, UTF_8_ENCODING, 2)
    } ?: UTF_8_ENCODING

    //是否多部件
    val isMultipart = MULTIPART_FORM_DATA_HEADER.equals(contentType, true)

    //传输数据为二进制类型时的 分割线
    val boundary = (isMultipart) trueDoBack {
        getDetailFromContentHeader(contentTypeHeader!!, BOUNDARY_PATTERN, null, 2)
    }

    init {
        contentTypeHeader?.let {
            Log.d("ContentType", it)
        }
    }

    //正则解析
    private fun getDetailFromContentHeader(
        contentTypeHeader: String,
        pattern: Pattern,
        defaultValue: String?,
        group: Int
    ): String? {
        val matcher = pattern.matcher(contentTypeHeader)
        return matcher.find() trueDoBack { matcher.group(group) } ?: defaultValue
    }
}