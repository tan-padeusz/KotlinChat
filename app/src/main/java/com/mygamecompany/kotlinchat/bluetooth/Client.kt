package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Client(bluetoothAdapter : BluetoothAdapter, context : Context): ChatDevice {

    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val message = String(characteristic!!.value)
            Timber.d("message=$message")
            lastMessage.postValue(message)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d("")
            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("SATE_CONNECTED: ")
                    //TODO("Implement connection message.")
                    scanner.switchConnectionValue(true)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    //TODO("Implement disconnection message.")
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("STATE_DISCONNECTED: ")
                    scanner.switchConnectionValue(false)
                    gatt?.close()
                }

                else -> { Timber.d("STATE_UNKNOWN: ") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d("")
            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("discovery success: ")
                    clientGatt = gatt
                    clientCharacteristic = clientGatt?.getService(Constants.serviceUUID)?.getCharacteristic(Constants.characteristicUUID)
                    if(clientCharacteristic == null) { Timber.d("discovery success: client characteristic is null") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("discovery failure: ") }
            }
        }
    }

    //CONSTANTS
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val scanner: Scanner = Scanner(bluetoothAdapter, context, gattClientCallback)

    //VARIABLES
    private var clientCharacteristic : BluetoothGattCharacteristic? = null
    private var clientGatt : BluetoothGatt? = null

    //FUNCTIONS
    override fun runDevice(enable: Boolean) {
        Timber.d("")
        if(enable) scanner.startScanning()
        else scanner.stopScanning()
    }

    override fun sendMessage(message: String) {
        Timber.d("")
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.sender + fullMessage)
        if(clientCharacteristic != null) {
            clientCharacteristic!!.setValue(Constants.receiver + fullMessage)
            Timber.d("result=${clientGatt?.writeCharacteristic(clientCharacteristic)}")
        } else { Timber.d("clientCharacteristic is null") }
    }

    override fun receiveMessage(): LiveData<String> {
        return lastMessage
    }

    private fun enableIndication(gatt : BluetoothGatt) {
        Timber.d("")
        val characteristic: BluetoothGattCharacteristic = gatt.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.descriptorUUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Timber.d("result=${gatt.writeDescriptor(descriptor)}")
    }
}