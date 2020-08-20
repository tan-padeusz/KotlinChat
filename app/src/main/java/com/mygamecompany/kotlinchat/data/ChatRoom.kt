package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothDevice

data class ChatRoom(val device: BluetoothDevice, val roomName: String)