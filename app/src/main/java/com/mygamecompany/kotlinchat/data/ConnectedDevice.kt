package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothDevice

data class ConnectedDevice(val username: String, val device: BluetoothDevice)