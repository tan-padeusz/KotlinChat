package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.ChatRoom
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.BLEDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEClient @Inject constructor (private val context: Context, private val scanner: Scanner): BLEDevice {
    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("Connected to server.")
                    isConnected.postValue(true)
                    gattClient = gatt
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("Disconnected from server.")
                    isConnected.postValue(false)
                    gattClient = null
                    gatt?.close()
                }

                else -> { Timber.d("Connection state changed: unknown connection state.") }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val receivedString = String(characteristic!!.value, Charsets.UTF_8)
            if (receivedString.length < 5) { Timber.d("Unknown message."); return }

            val code = receivedString.take(5)
            val message = receivedString.drop(5)
            when (code) {
                Constants.TEXT_MESSAGE -> dealWithTextMessage(message)
                Constants.CONNECTION_MESSAGE -> dealWithConnectionMessage(message)
                Constants.FORCE_DISCONNECTION_MESSAGE -> dealWithForceDisconnectionMessage()
                else -> dealWithOtherMessage(message)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Timber.d("Services discovery success.")
                    gatt?.requestMtu(512)
                }
                else -> { Timber.d("Services discovery failure.") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Timber.d("Mtu changed: $mtu")
            sendOwnUsername()
            enableIndication()
        }
    }

    //VALUES
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastConnectionMessage: MutableLiveData<String> = MutableLiveData()
    private val foundChatRooms: ArrayList<ChatRoom> = ArrayList()
    private val foundChatRoomsLiveData: MutableLiveData<ArrayList<ChatRoom>> = MutableLiveData()
    private val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)

    //VARIABLES
    private var gattClient : BluetoothGatt? = null

    //FUNCTIONS
    override fun run() {
        scanner.startScanning()
    }

    override fun stop() {
        scanner.stopScanning()
    }

    override fun getLastMessage(): LiveData<String> {
        return lastMessage
    }

    override fun getLastConnectionMessage(): LiveData<String> {
        return lastConnectionMessage
    }

    fun getFoundChatRooms(): LiveData<ArrayList<ChatRoom>> = foundChatRoomsLiveData
    fun isConnected(): LiveData<Boolean> = isConnected

    override fun sendMessage(message: String) {
        if (gattClient == null) return
        Timber.d("Sending message: $message")
        val characteristic = gattClient!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.WRITE_CHARACTERISTIC_UUID)
        characteristic?.value = (Constants.TEXT_MESSAGE + Repository.username + ":\n" + message).toByteArray(Charsets.UTF_8)
        Timber.d("Sending message result: ${gattClient!!.writeCharacteristic(characteristic)}")
    }

    private fun sendOwnUsername() {
        if (gattClient == null) return
        val characteristic = gattClient!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.WRITE_CHARACTERISTIC_UUID)
        characteristic?.value = (Constants.USERNAME_MESSAGE + Repository.username).toByteArray(Charsets.UTF_8)
        Timber.d("sendOwnUsername: result: ${gattClient!!.writeCharacteristic(characteristic)}")
    }

    private fun dealWithTextMessage(message: String) {
        lastMessage.postValue(message)
    }

    private fun dealWithConnectionMessage(message: String) {
        lastConnectionMessage.postValue(message)
    }

    private fun dealWithForceDisconnectionMessage() {
        disconnect()
    }

    private fun dealWithOtherMessage(message: String) {
        Timber.d("Received unknown message: $message")
    }

    fun connect(room: ChatRoom) {
        val serverDevice = room.device
        foundChatRooms.clear()
        Timber.d("Connecting to device with address: ${serverDevice.address}")
        serverDevice.connectGatt(context, false, gattClientCallback)
        scanner.stopScanning()
    }

    fun disconnect() {
        Timber.d("Disconnecting...")
        if (isConnected.value!!) gattClient!!.disconnect()
    }

    private fun enableIndication() {
        if (gattClient == null) { Timber.d("enableIndication: gatt is null."); return }
        Timber.d("Enabling indication...")
        val characteristic: BluetoothGattCharacteristic = gattClient!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        gattClient!!.setCharacteristicNotification(characteristic, true)
        val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(Constants.DESCRIPTOR_UUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        Timber.d("Enabling indication result: ${gattClient!!.writeDescriptor(descriptor)}")
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