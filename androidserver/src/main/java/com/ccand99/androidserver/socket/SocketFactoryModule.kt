package com.ccand99.androidserver.socket

import com.ccand99.androidserver.util.IFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.net.ServerSocket
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class SocketFactoryModule {

    @BindDefaultServerSocketFactory
    @Binds
    abstract fun bindDefaultServerSocket(defaultServerSocketFactory: DefaultServerSocketFactory): IFactory<ServerSocket>

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindDefaultServerSocketFactory