package com.mygamecompany.kotlinchat.data

import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import com.mygamecompany.kotlinchat.viewmodels.ChatViewModel
import com.mygamecompany.kotlinchat.viewmodels.RoomsViewModel
import java.lang.Exception
import javax.inject.Singleton

@Singleton
object Repository {
    //VARIABLES
    var username: String = ""
    var isServer: Boolean = false

    lateinit var client: BLEClient
    lateinit var server: BLEServer

    fun getClient(className: String): BLEClient {
        if (className != RoomsViewModel::class.java.simpleName && className != ChatViewModel::class.java.simpleName)
            throw Exception("Operation unavailable!")
        return client
    }

    fun getServer(className: String): BLEServer {
        if (className != ChatViewModel::class.java.simpleName)
            throw Exception("Operation unavailable!")
        return server
    }
}