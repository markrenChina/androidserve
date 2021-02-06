package com.ccand99.androidserver.ktfunction

import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

public fun Any?.safeClose(){
    try {
        this?.let {
            when(this){
                is Closeable -> this.close()
                is Socket -> this.close()
                is ServerSocket -> this.close()
                else -> throw IllegalArgumentException("Unknown object to close")
            }
        }
    }catch (e: Exception){
        Log.e("safeClose()", "Could not close",e)
    }
}

//isTrue 先执行var1 不能使用{var1}这种方式或者函数
public infix fun<T> Boolean.isTrue(var1 : T): T?= if (this) var1 else null

public inline infix fun Boolean.trueToDo(block: ()->Unit): Unit?= if (this) block() else null

public inline infix fun<T> Boolean.trueDoBack(block: ()-> T): T?= if (this) block() else null

fun decodePercent(str: String): String? {
    return try {
        //URLDecoder.decode(str,"UTF-8")
        URLDecoder.decode(str,"UTF8")
    }catch (ignored : UnsupportedEncodingException){
        Log.w("decodePercent", "Encoding not supported, ignored",ignored )
        null
    }
}

