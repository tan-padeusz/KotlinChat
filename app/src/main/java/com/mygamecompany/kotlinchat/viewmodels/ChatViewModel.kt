package com.mygamecompany.kotlinchat.viewmodels

import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.BLEDevice
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatViewModel @Inject constructor () {
    private val client: BLEClient = Repository.getClient(this::class.java.simpleName)
    private val server: BLEServer = Repository.getServer(this::class.java.simpleName)

    private fun getChatDevice(isServer: Boolean): BLEDevice = if (isServer) server else client

    fun getLastMessage(): LiveData<String> = getChatDevice(Repository.isServer).getLastMessage()
    fun getLastConnectionMessage(): LiveData<String> = getChatDevice(Repository.isServer).getLastConnectionMessage()
    fun sendMessage(message: String) = getChatDevice(Repository.isServer).sendMessage(message)

    /*CLIENT-ONLY METHOD*/
    fun disconnectFromServer() {
        if (Repository.isServer) { Timber.d("disconnectFromServer: action available only for client."); return }
        client.disconnect()
    }

    /*CLIENT-ONLY METHOD*/
    fun isConnectedToServer(): LiveData<Boolean>? {
        if (Repository.isServer) { Timber.d("isConnectedToServer: action available only for client."); return null }
        return client.isConnected()
    }

    /*SERVER-ONLY METHOD*/
    fun forceDisconnection() {
        if (Repository.isServer) { server.forceDisconnect(); return }
        Timber.d("forceDisconnect: action available only for server.")
    }
}