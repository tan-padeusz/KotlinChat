package com.mygamecompany.kotlinchat.interfaces

import androidx.lifecycle.LiveData

interface ChatDevice {
    fun runDevice(enable: Boolean)
    fun sendMessage(message: String)
    fun receiveMessage(): LiveData<String>
}