package com.ccand99.androidserver

import com.ccand99.androidserver.tempfiles.BindDefaultTempFileManager
import com.ccand99.androidserver.tempfiles.TempFileManager
import javax.inject.Inject

/**
 * ContentType ct = new ContentType(session.getHeaders().get("content-type")).tryUTF8();
   session.getHeaders().put("content-type", ct.getContentTypeHeader());
 */

//todo 后期修改主类继承为注入 ： class XXXX @Inject constructor()
public abstract class MicroHttp(val port : Int = 80,val hostname :String? =null) {

    //引入Hilt初始化
    @BindDefaultTempFileManager
    @Inject
    lateinit var tempFileManager: TempFileManager



    //@Inject lateinit var tempFileManagerFactory

    init {
        //setTempFileManagerFactory(DefaultTempFileManagerFactory())
        //setAsyncRunner(DefaultAsyncRunner())
    }

}