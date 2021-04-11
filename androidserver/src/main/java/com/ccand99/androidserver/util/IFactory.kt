package com.ccand99.androidserver.util

interface IFactory<T> {

    fun create(): T
}