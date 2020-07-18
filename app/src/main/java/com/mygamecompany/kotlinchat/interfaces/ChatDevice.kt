package com.mygamecompany.kotlinchat.interfaces

import android.net.Uri
import androidx.lifecycle.LiveData

interface ChatDevice {
    fun runBluetoothDevice(run: Boolean)
    fun getLastMessage(): LiveData<String>
    fun getLastConnectionMessage(): LiveData<String>
    fun getLastImageUri(): LiveData<Uri>
    fun sendMessage(message: String)
    fun sendConnectionMessage(connected: Boolean)
}