package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server

object Repository: ChatDevice {

    var isServer: Boolean = false
    var username: String = ""

    private var client: Client? = null
    private var server: Server? = null

    fun initializeBluetoothActions(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    override fun runDevice(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun sendMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun receiveMessage(): LiveData<String> {
        TODO("Not yet implemented")
    }
}
