package com.ccand99.androidserver


import com.ccand99.androidserver.response.ResponseStatus


class ResponseException(
    public val statu: ResponseStatus,
    override val message: String,
    val e: Exception? = null
) : Exception(message, e) {

    companion object {
        private const val serialVersionUID = Long.MAX_VALUE - 11111L
    }
}