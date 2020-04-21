package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber
import java.util.*

class Server(bluetoothAdapter : BluetoothAdapter, context : Context): ChatDevice {

    //SERVER CALLBACK
    private val gattServerCallback : BluetoothGattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Timber.d("")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("STATE_CONNECTED: ")
                    //TODO("Implement connection message.")
                }
                BluetoothProfile.STATE_DISCONNECTING-> {
                    Timber.d("STATE_DISCONNECTING")
                    //TODO(Implement disconnection message.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("STATE_DISCONNECTED: ")
                    for (client in connectedDevices) connectedDevices.remove(client)
                }
                else -> { Timber.d("STATE_UNKNOWN: ") }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            Timber.d("")
            val message = String(value as ByteArray)
            val status: Int = when(message[0]) {
                Constants.clientConnectionMessage -> Constants.connectionMessage
                Constants.receiver -> Constants.textMessage
                else -> Constants.messageNotReceived
            }
            Timber.d("status=$status ; message=$message")
            lastMessage.postValue(message)
            val serverCharacteristic: BluetoothGattCharacteristic = advertiser.getServerCharacteristic()
            serverCharacteristic.setValue(message)
            for (client in connectedDevices) if (client.address != device?.address) notifyDevice(client, serverCharacteristic)
            if(responseNeeded) advertiser.getGattServer()!!.sendResponse(device, requestId, status, offset, byteArrayOf('0'.toByte()))
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            Timber.d("")
            advertiser.getGattServer()?.sendResponse(device, requestId, 0, offset, value)
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Timber.d("mtu=$mtu")
        }
    }

    //CONSTANTS
    private val advertiser: Advertiser = Advertiser(bluetoothAdapter, context, gattServerCallback)
    private val connectedDevices: LinkedList<BluetoothDevice> = LinkedList()
    private val lastMessage: MutableLiveData<String> = MutableLiveData()

    //FUNCTIONS
    override fun runDevice(enable: Boolean) {
        Timber.d("")
        if(enable) advertiser.startAdvertising()
        else advertiser.stopAdvertising()
    }

    override fun sendMessage(message: String) {
        Timber.d("")
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.sender + fullMessage)
        val characteristic = advertiser.getServerCharacteristic()
        characteristic.setValue(Constants.receiver + fullMessage)
        for(device in connectedDevices) notifyDevice(device, characteristic)
    }

    override fun receiveMessage(): LiveData<String> {
        Timber.d("")
        return lastMessage
    }

    private fun notifyDevice(client: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        Timber.d("")
        advertiser.getGattServer()!!.notifyCharacteristicChanged(client, characteristic, false)
    }
}