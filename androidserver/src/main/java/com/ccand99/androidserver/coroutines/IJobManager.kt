package com.ccand99.androidserver.coroutines

import kotlinx.coroutines.Job

/**
 * Job for asynchronously executing requests.
 */
interface IJobManager {
    //TODO ClientHandler作为函数，或者job传入
    fun closeAll()
    fun closed(clientJob: Job)
    fun exec(clientJob: Job)
}