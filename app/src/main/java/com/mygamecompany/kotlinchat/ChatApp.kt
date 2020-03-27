package com.mygamecompany.kotlinchat

import android.app.Application
import timber.log.Timber

class ChatApp: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.d("Timber set up!")
    }
}