package com.mygamecompany.kotlinchat.utilities

import android.os.ParcelUuid
import java.util.*

object Constants
{
    val serviceUUID : UUID = UUID.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val parcelServiceUUID: ParcelUuid = ParcelUuid.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val characteristicUUID: UUID = UUID.fromString("0000ABCD-0000-1000-8000-00805F9B34FB")
    val descriptorUUID: UUID = UUID.fromString("0000DCBA-0000-1000-8000-00805F9B34FB")

    const val connection: Char = '0'
    const val sender: Char = '1'
    const val receiver: Char = '2'

    const val connectionMessage: Int = 0
    const val messageNotReceived: Int = 1
    const val textMessage: Int = 2
}
