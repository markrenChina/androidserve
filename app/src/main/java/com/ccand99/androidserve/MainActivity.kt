package com.ccand99.androidserve

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ccand99.androidserver.tempfiles.DefaultTempFileManager
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.getProperty("java.io.tmpdir")?.let {
            println(it)
        } ?: println("null")



        PermissionX.init(this)
            .permissions(Manifest.permission.INTERNET)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted){
                    try{
                        //TestServer(8888)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
    }

}