package com.mygamecompany.kotlinchat.viewmodels

import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import com.mygamecompany.kotlinchat.data.ChatRoom
import com.mygamecompany.kotlinchat.data.Repository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomsViewModel @Inject constructor() {
    private val client: BLEClient = Repository.getClient(this::class.java.simpleName)

    /*CLIENT-ONLY METHOD*/
    fun getFoundChatRooms(): LiveData<ArrayList<ChatRoom>>?  {
        if (Repository.isServer) { Timber.d("getFoundChatRooms: action available only for client."); return null }
        return client.getFoundChatRooms()
    }

    /*CLIENT-ONLY METHOD*/
    fun connectToServer(room: ChatRoom) {
        if (Repository.isServer) { Timber.d("connectToServer: action available only for client."); return }
        client.connect(room)
    }

    /*CLIENT-ONLY METHOD*/
    fun isConnectedToServer(): LiveData<Boolean>? {
        if (Repository.isServer) { Timber.d("isConnectedToServer: action available only for client."); return null }
        return client.isConnected()
    }
}