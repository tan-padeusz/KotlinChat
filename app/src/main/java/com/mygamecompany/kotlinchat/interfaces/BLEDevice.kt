package com.mygamecompany.kotlinchat.interfaces

import androidx.lifecycle.LiveData

interface BLEDevice {
    fun run() {  }
    fun stop() {  }
    fun getLastMessage(): LiveData<String>
    fun getLastConnectionMessage(): LiveData<String>
    fun sendMessage(message: String)
}