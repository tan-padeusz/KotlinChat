package com.mygamecompany.kotlinchat.viewmodels

import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import com.mygamecompany.kotlinchat.data.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuViewModel @Inject constructor (private val client: BLEClient, private val server: BLEServer) {
//    @Inject lateinit var client: BLEClient
//    @Inject lateinit var server: BLEServer

    fun runBLEDevice() {
        if (Repository.isServer) server.run()
        else client.run()

        Repository.client = client
        Repository.server = server
    }

    fun stopBLEDevice() {
        if (Repository.isServer) server.stop()
        else client.stop()
    }
}