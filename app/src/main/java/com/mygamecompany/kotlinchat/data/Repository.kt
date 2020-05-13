package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import com.mygamecompany.kotlinchat.interfaces.ChatDevice

object Repository: ChatDevice {

    const val TAG: String = "KTC"

    var isServer: Boolean = false
    var username: String = ""

    private lateinit var client: Client
    private lateinit var server: Server

    fun initializeBluetoothDevices(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    override fun runDevice(enable: Boolean) {
        if(isServer) server.runDevice(enable)
        else client.runDevice(enable)
    }

    override fun sendMessage(message: String) {
        if(isServer) server.sendMessage(message)
        else client.sendMessage(message)
    }

    override fun receiveMessage(): LiveData<String> {
        return if(isServer) server.receiveMessage()
        else client.receiveMessage()
    }
}
