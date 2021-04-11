package com.ccand99.androidserver.tempfiles

import com.ccand99.androidserver.util.IFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class TempFileModule {

    @BindDefaultTempFileManager
    @Binds
    abstract fun bindDefaultTempFileManager(defaultTempFileManager: DefaultITempFileManager): ITempFileManager

    @BindDefaultTempFileManagerFactory
    @Binds
    abstract fun bindDefaultTempFileManagerFactory(defaultTempFileManagerFactory: DefaultTempFileManagerFactory): IFactory<ITempFileManager>

    /*@BindCustomTempFileManager
    @Binds
    abstract fun bindCustomTempFileManager():TempFileManager*/
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindDefaultTempFileManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindDefaultTempFileManagerFactory

/*
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindCustomTempFileManager*/
