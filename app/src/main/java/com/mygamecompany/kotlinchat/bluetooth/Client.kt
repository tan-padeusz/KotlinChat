package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.data.Repository.TAG
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Client(bluetoothAdapter : BluetoothAdapter, context : Context): ChatDevice {

    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val message = String(characteristic!!.value)
            Timber.d("$TAG: onCharacteristicChanged: message=$message")
            lastMessage.postValue(message)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d("$TAG: onConnectionStateChange:")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("$TAG: onConnectionStateChange: SATE_CONNECTED:")
                    scanner.switchConnectionValue(true)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("$TAG: onConnectionStateChange: STATE_DISCONNECTED:")
                    scanner.switchConnectionValue(false)
                    gatt?.close()
                }

                else -> { Timber.d("$TAG: onConnectionStateChange: STATE_UNKNOWN:") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d("$TAG: onMtuChanged:")
            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("$TAG: onServicesDiscovered:")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("$TAG: onServicesDiscovered: discovery success:")
                    clientGatt = gatt
                    clientCharacteristic = clientGatt?.getService(Constants.serviceUUID)?.getCharacteristic(Constants.characteristicUUID)
                    if(clientCharacteristic == null) { Timber.d("$TAG: onServicesDiscovered: discovery success: client characteristic is null") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("$TAG: onServicesDiscovered: discovery failure:") }
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
        Timber.d("$TAG: runDevice: enable=$enable")
        if(enable) scanner.startScanning()
        else scanner.stopScanning()
    }

    override fun sendMessage(message: String) {
        Timber.d("$TAG: sendMessage:")
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.TEXT_MESSAGE_SENDER + fullMessage)
        clientCharacteristic?.setValue(Constants.TEXT_MESSAGE_RECEIVER + fullMessage) ?:
            Timber.d("$TAG: sendMessage: clientCharacteristic is null!")
        Timber.d("$TAG: sendMessage: result=${clientGatt?.writeCharacteristic(clientCharacteristic)}")
    }

    override fun receiveMessage(): LiveData<String> {
        Timber.d("$TAG: receiveMessage:")
        return lastMessage
    }

    private fun enableIndication(gatt : BluetoothGatt) {
        Timber.d("$TAG: enableIndication:")
        val characteristic: BluetoothGattCharacteristic = gatt.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.descriptorUUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Timber.d("$TAG: enableIndication: result=${gatt.writeDescriptor(descriptor)}")
    }
}