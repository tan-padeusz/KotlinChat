package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothDevice

data class ChatRoom(val device: BluetoothDevice, val roomName: String) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        return this.device.address == (other as ChatRoom).device.address
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + roomName.hashCode()
        return result
    }
}