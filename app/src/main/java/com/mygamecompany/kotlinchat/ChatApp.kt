package com.mygamecompany.kotlinchat

import android.app.Application
import com.mygamecompany.kotlinchat.log.DebugTree
import timber.log.Timber

@Suppress("unused")
class ChatApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        Timber.d("Timber set up!")
    }
}