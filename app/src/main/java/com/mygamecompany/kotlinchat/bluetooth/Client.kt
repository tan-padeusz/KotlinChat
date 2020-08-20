package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import android.net.Uri
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.ChatRoom
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Client(bluetoothAdapter: BluetoothAdapter, private val context: Context): ChatDevice {
    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val receivedString = String(characteristic!!.value)
            if (receivedString.length < 5) { Timber.d("Unknown message."); return; }

            val code = receivedString.take(5)
            val message = receivedString.drop(5)
            when (code) {
                Constants.TEXT_MESSAGE -> dealWithTextMessage(message)
                Constants.CONNECTION_MESSAGE -> dealWithConnectionMessage(message)
                Constants.FORCE_DISCONNECTION_MESSAGE -> dealWithForceDisconnectionMessage()
                else -> dealWithOtherMessage(message)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("Connection state changed. New state: SATE_CONNECTED.")
                    isConnected.postValue(true)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("Connection state changed. New state: STATE_DISCONNECTED.")
                    isConnected.postValue(false)
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
    private val foundChatRooms: ArrayList<ChatRoom> = ArrayList()
    private val foundChatRoomsLiveData: MutableLiveData<ArrayList<ChatRoom>> = MutableLiveData()
    private val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val wasForciblyDisconnected: MutableLiveData<Boolean> = MutableLiveData()

    //VARIABLES
    private var writeCharacteristic : BluetoothGattCharacteristic? = null
    private var clientGatt : BluetoothGatt? = null

    //FUNCTIONS
    override fun runBluetoothDevice(run: Boolean) = if (run) scanner.startScanning() else scanner.stopScanning()
    override fun getLastMessage(): LiveData<String> = lastMessage
    override fun getLastConnectionMessage(): LiveData<String> = lastConnectionMessage
    fun getFoundChatRooms(): LiveData<ArrayList<ChatRoom>> = foundChatRoomsLiveData
    fun isConnected(): LiveData<Boolean> = isConnected
    fun wasForciblyDisconnected(): LiveData<Boolean> = wasForciblyDisconnected

    override fun sendMessage(message: String) {
        if (clientGatt == null) return
        Timber.d("Sending message: $message")
        writeCharacteristic?.value = "${Constants.TEXT_MESSAGE}${Repository.username}:\n${message}".toByteArray(Charsets.UTF_8)
        Timber.d("Sending message result: ${clientGatt!!.writeCharacteristic(writeCharacteristic)}")
    }

    private fun sendOwnUsername() {
        if (clientGatt == null) return
        writeCharacteristic?.value = (Constants.USERNAME_MESSAGE + Repository.username).toByteArray(Charsets.UTF_8)
        Timber.d("Sending username result: ${clientGatt!!.writeCharacteristic(writeCharacteristic)}")
    }

    override fun sendConnectionMessage(connected: Boolean) {
        TODO("Not yet implemented")
    }

    fun connect(position: Int) {
        val serverDevice = foundChatRooms[position].device
//        with(foundChatRooms) {
//            for (possibleClient in this) this.remove(possibleClient)
//            foundChatRoomsLiveData.postValue(this)
//        }
        Timber.d("Connecting to device with address: ${serverDevice.address}")
        serverDevice.connectGatt(context, false, gattClientCallback)
        scanner.stopScanning()
    }

    private fun dealWithTextMessage(message: String) {
        lastMessage.postValue(message)
    }

    private fun dealWithConnectionMessage(message: String) {
        lastConnectionMessage.postValue(message)
    }

    private fun dealWithForceDisconnectionMessage() {
        disconnect(true)
    }

    private fun dealWithOtherMessage(message: String) {
        Timber.d("Received unknown message: $message")
    }

    fun disconnect(forced: Boolean) {
        Timber.d("Disconnecting... forced: $forced")
        if (isConnected.value!!) {
            clientGatt?.disconnect()
            if (forced) wasForciblyDisconnected.postValue(true)
            isConnected.postValue(false)
            clientGatt?.close()
        }
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
            val room = ChatRoom(it.device, it.scanRecord?.serviceData?.get(ParcelUuid(Constants.SERVICE_UUID))!!.toString(Charsets.UTF_8))
            if (!foundChatRooms.contains(room)) {
                Timber.d("New room added to list.")
                foundChatRooms.add(room)
                foundChatRoomsLiveData.postValue(foundChatRooms)
            }
        }
    }
}