package com.mygamecompany.kotlinchat.log

import timber.log.Timber

class DebugTree: Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "KTCH_$tag", message, t)
    }
}