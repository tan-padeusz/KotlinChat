package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import timber.log.Timber

object Repository {
    //VARIABLES
    var username: String = ""
    var isServer: Boolean = false
    private var bleClient: BLEClient? = null
    private var bleServer: BLEServer? = null

    //FUNCTIONS

    fun initializeBluetoothDevices(bluetoothAdapter: BluetoothAdapter, context: Context) {
        bleClient = BLEClient(bluetoothAdapter, context)
        bleServer = BLEServer(bluetoothAdapter, context)
    }

    fun runBLEDevice() {
        if (isServer) bleServer!!.run()
        else bleClient!!.run()
    }

    fun stopBLEDevice() {
        if (isServer) bleServer!!.stop()
        else bleClient!!.stop()
    }

    fun getLastMessage(): LiveData<String> {
        return if (isServer) bleServer!!.getLastMessage()
        else bleClient!!.getLastMessage()
    }

    fun getLastConnectionMessage(): LiveData<String> {
        return if (isServer) bleServer!!.getLastConnectionMessage()
        else bleClient!!.getLastConnectionMessage()
    }

    fun sendMessage(message: String) {
        if (isServer) bleServer!!.sendMessage(message)
        else bleClient!!.sendMessage(message)
    }

    /*CLIENT-ONLY METHOD*/
    fun getFoundChatRooms(): LiveData<ArrayList<ChatRoom>>?  {
        if (isServer) { Timber.d("getFoundChatRooms: action available only for client."); return null }
        return bleClient!!.getFoundChatRooms()
    }

    /*CLIENT-ONLY METHOD*/
    fun connectToServer(position: Int) {
        if (isServer) { Timber.d("connectToServer: action available only for client."); return }
        bleClient!!.connect(position)
    }

    /*CLIENT-ONLY METHOD*/
    fun disconnectFromServer() {
        if (isServer) { Timber.d("disconnectFromServer: action available only for client."); return }
        bleClient!!.disconnect()
    }

    /*CLIENT-ONLY METHOD*/
    fun isConnectedToServer(): LiveData<Boolean>? {
        if (isServer) { Timber.d("isConnectedToServer: action available only for client."); return null }
        return bleClient!!.isConnected()
    }

    /*SERVER-ONLY METHOD*/
    fun forceDisconnection() {
        if (isServer) { bleServer!!.forceDisconnect(); return }
        Timber.d("forceDisconnect: action available only for server.")
    }
}