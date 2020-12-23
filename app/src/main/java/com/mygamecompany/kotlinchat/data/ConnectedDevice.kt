package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothDevice

data class ConnectedDevice(val username: String, val device: BluetoothDevice) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        return this.device.address == (other as ConnectedDevice).device.address
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + device.hashCode()
        return result
    }
}