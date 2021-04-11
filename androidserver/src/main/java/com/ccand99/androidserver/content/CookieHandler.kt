package com.ccand99.androidserver.content

import javax.inject.Inject

/**
 * Provides rudimentary support for cookies.
 * Doesn't support 'path', 'secure' nor 'httpOnly'.
 * Feel free to improve it and/or add unsupported features.
 *
 * 提供对cookie的基本支持。不支持“path”、“secure”和“httpOnly”。
 * 您可以随意改进它和/或添加不受支持的特性。
 *
 * @author markrenChina
 */
class CookieHandler @Inject constructor() {

    private val cookies = HashMap<String, String>()

}