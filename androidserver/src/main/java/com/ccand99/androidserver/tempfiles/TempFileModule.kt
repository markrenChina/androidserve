package com.ccand99.androidserver.tempfiles

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
    abstract fun bindDefaultTempFileManager(defaultTempFileManager: DefaultTempFileManager ):TempFileManager

    /*@BindCustomTempFileManager
    @Binds
    abstract fun bindCustomTempFileManager():TempFileManager*/
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindDefaultTempFileManager

/*
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindCustomTempFileManager*/
