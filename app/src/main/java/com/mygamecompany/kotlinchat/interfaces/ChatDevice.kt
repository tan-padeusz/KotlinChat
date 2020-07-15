package com.mygamecompany.kotlinchat.interfaces

import androidx.lifecycle.LiveData

interface ChatDevice {
    fun runBluetoothDevice(run: Boolean)
    fun sendMessage(message: String)
    fun receiveMessage(): LiveData<String>
}