package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import timber.log.Timber

object Repository : ChatDevice {
    //VARIABLES
    var isServer: Boolean = false
    var username: String = ""
    private var client: Client? = null
    private var server: Server? = null

    //FUNCTIONS
    override fun getLastMessage(): LiveData<String> = if (isServer) server!!.getLastMessage() else client!!.getLastMessage()
    override fun getLastConnectionMessage(): LiveData<String> = if (isServer) server!!.getLastConnectionMessage() else client!!.getLastConnectionMessage()
    override fun sendMessage(message: String) = if (isServer) server!!.sendMessage(message) else client!!.sendMessage(message)
    override fun sendConnectionMessage(connected: Boolean) = if (isServer) server!!.sendConnectionMessage(connected) else client!!.sendConnectionMessage(connected)

    fun initializeBluetoothDevices(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    override fun runBluetoothDevice(run: Boolean) {
        if (isServer) server!!.runBluetoothDevice(run)
        else client!!.runBluetoothDevice(run)
    }

    fun getFoundChatRooms(): LiveData<ArrayList<ChatRoom>>?  {
        if (isServer) { Timber.d("Could not get chat rooms. Device is not client."); return null; }
        return client!!.getFoundChatRooms()
    }

    fun connectToServer(position: Int) {
        if (isServer) { Timber.d("Could not connect server device to server."); return; }
        client!!.connect(position)
    }

    fun disconnectFromServer() {
        if (isServer) { Timber.d("Cannot disconnect server from server."); return; }
        client!!.disconnect(false)
    }

    fun isConnectedToServer(): LiveData<Boolean>? {
        if (isServer) { Timber.d("Cannot track server connection status."); return null; }
        return client!!.isConnected()
    }

    fun wasForcedConnection(): LiveData<Boolean>? {
        if (isServer) { Timber.d("Cannot track server connection status."); return null; }
        return client!!.wasForciblyDisconnected()
    }
}