package com.mygamecompany.kotlinchat.utilities

import android.os.ParcelUuid
import java.util.*

class Constants
{
    companion object
    {
        private var instance: Constants? = null
        fun getInstance(): Constants
        {
            if (instance == null) instance = Constants()
            return instance!!
        }
    }

    val serviceUUID : UUID = UUID.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val parcelServiceUUID : ParcelUuid = ParcelUuid.fromString("0000B87C-0000-1000-8000-00805F9B34FB")
    val characteristicUUID : UUID = UUID.fromString("0000ABCD-0000-1000-8000-00805F9B34FB")
    val descriptorUUID : UUID = UUID.fromString("0000DCBA-0000-1000-8000-00805F9B34FB")

    //val ping : Char = '0'
    val message : Char = '1'

    val messageNotReceived = 0
    val messageReceived = 1
}