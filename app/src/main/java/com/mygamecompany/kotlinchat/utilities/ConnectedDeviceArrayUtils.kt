package com.mygamecompany.kotlinchat.utilities

import android.bluetooth.BluetoothDevice
import com.mygamecompany.kotlinchat.data.ConnectedDevice

fun ArrayList<ConnectedDevice>.containsDevice(device: BluetoothDevice): Boolean {
    for (client in this) if (client.device == device) return true
    return false
}

fun ArrayList<ConnectedDevice>.removeDevice(device: BluetoothDevice): Boolean {
    return this.removeIf { client -> client.device == device }
}

fun ArrayList<ConnectedDevice>.getUsernameByDevice(device: BluetoothDevice): String {
    for (client in this) if (client.device == device) return client.username
    return ""
}