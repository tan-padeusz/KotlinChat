package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.ScanResult
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
            Timber.d("onCharacteristicChanged: message=$message")
            lastMessage.postValue(message)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d("onConnectionStateChange:")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("onConnectionStateChange: SATE_CONNECTED:")
                    scanner.switchConnectionValue(true)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("onConnectionStateChange: STATE_DISCONNECTED:")
                    scanner.switchConnectionValue(false)
                    gatt?.close()
                }

                else -> { Timber.d("onConnectionStateChange: STATE_UNKNOWN:") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d("onMtuChanged:")
            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("onServicesDiscovered:")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("onServicesDiscovered: discovery success:")
                    clientGatt = gatt
                    clientCharacteristic = clientGatt?.getService(Constants.SERVICE_UUID)?.getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
                    if(clientCharacteristic == null) { Timber.d("onServicesDiscovered: discovery success: client characteristic is null") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("onServicesDiscovered: discovery failure:") }
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
    override fun runBluetoothDevice(run: Boolean) {
        Timber.d("runDevice: enable=$run")
        if(run) scanner.startScanning()
        else scanner.stopScanning()
    }

    override fun sendMessage(message: String) {
        Timber.d("sendMessage:")
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.TEXT_MESSAGE_SENDER + fullMessage)
        clientCharacteristic?.setValue(Constants.TEXT_MESSAGE_RECEIVER + fullMessage) ?:
            Timber.d("sendMessage: clientCharacteristic is null!")
        Timber.d("sendMessage: result=${clientGatt?.writeCharacteristic(clientCharacteristic)}")
    }

    override fun receiveMessage(): LiveData<String> {
        Timber.d("receiveMessage:")
        return lastMessage
    }

    private fun enableIndication(gatt : BluetoothGatt) {
        Timber.d("enableIndication:")
        val characteristic: BluetoothGattCharacteristic = gatt.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.DESCRIPTOR_UUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Timber.d("enableIndication: result=${gatt.writeDescriptor(descriptor)}")
    }
    
    fun getLastScanResult(): LiveData<ScanResult> = scanner.getLastScanResult()
}