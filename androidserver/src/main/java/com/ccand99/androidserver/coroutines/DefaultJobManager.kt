package com.ccand99.androidserver.coroutines

import kotlinx.coroutines.Job
import java.util.*
import kotlin.collections.ArrayList

class DefaultJobManager : IJobManager {

    var requestCount: Long = 0

    private val jobs: MutableList<Job> = Collections.synchronizedList(ArrayList<Job>())


    override fun closeAll() {
        jobs.forEach { job ->
            job.cancel()
        }
    }

    override fun closed(clientJob: Job) {
        jobs.remove(clientJob)
    }

    override fun exec(clientJob: Job) {
        ++this.requestCount
        jobs.add(clientJob)
    }
}