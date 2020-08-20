package com.mygamecompany.kotlinchat.utilities

import java.util.*

object Constants {
    val SERVICE_UUID : UUID = UUID.fromString("4dc7b28e-47cf-457d-bf02-6d663633b8b2")
    val READ_CHARACTERISTIC_UUID: UUID = UUID.fromString("42c30079-cafa-4f8b-a7ca-f866d50ae030")
    val WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("5362407f-8537-49c4-85f6-9aa66f7edd10")
    val DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    const val TEXT_MESSAGE: String = "#MSG#"
    const val CONNECTION_MESSAGE: String = "#CNC#"
    const val USERNAME_MESSAGE: String = "#USN#"
    const val FORCE_DISCONNECTION_MESSAGE: String = "#FDC#"

    const val OTHER_MESSAGE_STATUS: Int = -1
    const val TEXT_MESSAGE_STATUS: Int = 0
    const val CONNECTION_MESSAGE_STATUS: Int = 1
}