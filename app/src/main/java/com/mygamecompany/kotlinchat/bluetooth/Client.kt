package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Client(bluetoothAdapter : BluetoothAdapter, private val context : Context): ChatDevice {

    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val message = String(characteristic!!.value)
            Timber.d("Received new message: $message")
            if (message[0] == Constants.CONNECTION_MESSAGE) lastConnectionMessage.postValue(message.takeLast(message.length - 1))
            else lastMessage.postValue(message)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("Connection state changed. New state: SATE_CONNECTED.")
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("Connection state changed. New state: STATE_DISCONNECTED.")
                    gatt?.close()
                    clientGatt = null
                }

                else -> { Timber.d("Connection state changed. New state: STATE_UNKNOWN.") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d("Mtu changed: $mtu")
            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("Services discovery success.")
                    clientGatt = gatt
                    writeCharacteristic = clientGatt?.getService(Constants.SERVICE_UUID)?.getCharacteristic(Constants.WRITE_CHARACTERISTIC_UUID)
                    if(writeCharacteristic == null) { Timber.d("Service discovery success, but characteristic was null.") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("Services discovery failure.") }
            }
        }
    }

    //VALUES
    private val scanner: Scanner = Scanner(bluetoothAdapter)
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastConnectionMessage: MutableLiveData<String> = MutableLiveData()
    private val lastImageUri: MutableLiveData<Uri> = MutableLiveData()
    private val foundChatRooms: ArrayList<ScanResult> = ArrayList()
    private val foundChatRoomsLiveData: MutableLiveData<ArrayList<ScanResult>> = MutableLiveData()

    //VARIABLES
    private var writeCharacteristic : BluetoothGattCharacteristic? = null
    private var clientGatt : BluetoothGatt? = null

    //FUNCTIONS
    override fun runBluetoothDevice(run: Boolean) = if (run) scanner.startScanning() else scanner.stopScanning()
    override fun getLastMessage(): LiveData<String> = lastMessage
    override fun getLastConnectionMessage(): LiveData<String> = lastConnectionMessage
    override fun getLastImageUri(): LiveData<Uri> = lastImageUri
    fun getFoundChatRooms(): LiveData<ArrayList<ScanResult>> = foundChatRoomsLiveData

    override fun sendMessage(message: String) {
        Timber.d("Sending message: $message")
        writeCharacteristic?.setValue("${Constants.TEXT_MESSAGE}${Repository.username}:\n${message}") ?:
            Timber.d("sendMessage: clientCharacteristic is null!")
        Timber.d("sendMessage: result=${clientGatt?.writeCharacteristic(writeCharacteristic)}")
    }

    override fun sendConnectionMessage(connected: Boolean) {
        TODO("Not yet implemented")
    }

    fun connect(position: Int) {
        val device = foundChatRooms[position].device
        with(foundChatRooms) {
            for (possibleClient in this) this.remove(possibleClient)
            foundChatRoomsLiveData.postValue(this)
        }
        Timber.d("Connecting to device with address: ${device.address}")
        device.connectGatt(context, false, gattClientCallback)
        scanner.stopScanning()
    }

    private fun enableIndication(gatt : BluetoothGatt) {
        Timber.d("Enabling indication...")
        val characteristic: BluetoothGattCharacteristic = gatt.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.DESCRIPTOR_UUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        Timber.d("Enabling indication result: ${gatt.writeDescriptor(descriptor)}")
    }
    
    init {
        scanner.getLastScanResult().observeForever {
            if (!foundChatRooms.contains(it)) {
                foundChatRooms.add(it)
                foundChatRoomsLiveData.postValue(foundChatRooms)
            }
        }
    }
}