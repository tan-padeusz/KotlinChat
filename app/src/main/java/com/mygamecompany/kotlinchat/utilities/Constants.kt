package com.mygamecompany.kotlinchat.utilities

import android.os.ParcelUuid
import java.util.*

object Constants {
    val serviceUUID : UUID = UUID.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val parcelServiceUUID: ParcelUuid = ParcelUuid.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val characteristicUUID: UUID = UUID.fromString("0000ABCD-0000-1000-8000-00805F9B34FB")
    val descriptorUUID: UUID = UUID.fromString("0000DCBA-0000-1000-8000-00805F9B34FB")

    const val TEXT_MESSAGE_SENDER: Char = '0'
    const val TEXT_MESSAGE_RECEIVER: Char = '1'
    const val CONNECTION_MESSAGE: Char = '2'
    const val DISCONNECTION_MESSAGE = '3'

    const val OTHER_MESSAGE_STATUS: Int = -1
    const val TEXT_MESSAGE_STATUS: Int = 0
    const val CONNECTION_MESSAGE_STATUS: Int = 1
    const val DISCONNECTION_MESSAGE_STATUS: Int = 2
}