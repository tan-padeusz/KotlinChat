package com.mygamecompany.kotlinchat

import android.app.Application
import com.mygamecompany.kotlinchat.data.Repository
import timber.log.Timber

class ChatApp: Application() {
    private val appTag: String = Repository.TAG

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.d("$appTag:Timber set up!")
    }
}