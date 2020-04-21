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
            Timber.d("${Repository}: message=$message")
            lastMessage.postValue(message)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d(Repository.toString())
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("${Repository}: SATE_CONNECTED: ")
                    scanner.switchConnectionValue(true)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("${Repository}: STATE_DISCONNECTED: ")
                    scanner.switchConnectionValue(false)
                    gatt?.close()
                }

                else -> { Timber.d("${Repository}: STATE_UNKNOWN: ") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d(Repository.toString())
            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d(Repository.toString())
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("${Repository}: discovery success: ")
                    clientGatt = gatt
                    clientCharacteristic = clientGatt?.getService(Constants.serviceUUID)?.getCharacteristic(Constants.characteristicUUID)
                    if(clientCharacteristic == null) { Timber.d("${Repository}: discovery success: client characteristic is null") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("${Repository}: discovery failure: ") }
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
        Timber.d("${Repository}: enable=$enable")
        if(enable) scanner.startScanning()
        else scanner.stopScanning()
    }

    override fun sendMessage(message: String) {
        Timber.d(Repository.toString())
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.TEXT_MESSAGE_SENDER + fullMessage)
        clientCharacteristic?.setValue(Constants.TEXT_MESSAGE_RECEIVER + fullMessage) ?:
            Timber.d("${Repository}: clientCharacteristic is null!")
        Timber.d("$Repository result=${clientGatt?.writeCharacteristic(clientCharacteristic)}")
    }

    override fun receiveMessage(): LiveData<String> {
        return lastMessage
    }

    private fun enableIndication(gatt : BluetoothGatt) {
        Timber.d(Repository.toString())
        val characteristic: BluetoothGattCharacteristic = gatt.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.descriptorUUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Timber.d("${Repository}: result=${gatt.writeDescriptor(descriptor)}")
    }

//    private fun sendConnectionMessage(connected: Boolean) {
//        Timber.d(Repository.toString())
//        val code = if(connected) Constants.CONNECTION_MESSAGE
//        else Constants.DISCONNECTION_MESSAGE
//        clientCharacteristic?.setValue(code + Repository.username) ?:
//                Timber.d("${Repository}: client characteristic is null!")
//        Timber.d("${Repository}: result=${clientGatt?.writeCharacteristic(clientCharacteristic)}")
//    }
}