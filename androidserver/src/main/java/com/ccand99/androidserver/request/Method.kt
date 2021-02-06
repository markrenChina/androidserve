package com.ccand99.androidserver.request

/**
 * HTTP Request methods, with the ability to decode a String back to its enum value.
 * HTTP请求方法，具有将字符串解码回其enum值的能力。
 * @author markrenChina
 */
enum class Method {
    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,
    PATCH,
    PROPFIND,
    PROPPATCH,
    MKCOL,
    MOVE,
    COPY,
    LOCK,
    UNLOCK;

    companion object {
        fun lookup(method: String?): Method? =
            method?.let {
                try {
                    valueOf(method)
                }catch (e : IllegalArgumentException){
                    null
                }
            }

    }

}