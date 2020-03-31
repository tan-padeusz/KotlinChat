package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.lifecycle.LiveData
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import timber.log.Timber

object Repository {
    enum class DeviceRole {
        NONE,
        CLIENT,
        SERVER
    }

    private var deviceRole: DeviceRole = DeviceRole.NONE
    private var userName: String = ""

    private var client: Client? = null
    private var server: Server? = null

    fun initializeBluetoothActions(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    fun setUserName(userName: String) {
        this.userName = userName
    }

    fun changeDeviceRole(newRole: DeviceRole) {
        deviceRole = newRole
    }

    fun sendMessage(message: String) {
        when(deviceRole) {
            DeviceRole.CLIENT -> client!!.sendMessageToServer(userName, message)
            DeviceRole.SERVER -> server!!.sendMessageToClient(userName, message)
            DeviceRole.NONE -> Timber.d("Device role was not set!")
        }
    }

    fun receiveMessage(): LiveData<String> {
        throw NotImplementedError("receiveMessage method was not yet implemented!")
    }
}
