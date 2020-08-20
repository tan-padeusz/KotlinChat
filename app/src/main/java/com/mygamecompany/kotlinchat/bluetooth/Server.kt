package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber
import java.util.*

class Server(bluetoothAdapter : BluetoothAdapter, private val context : Context): ChatDevice {
    //SERVER CALLBACK
    private val gattServerCallback : BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("Connection state changed. Device at address (${device?.address}) has connected.")
                    if(!connectedDevices.contains(device)) connectedDevices.add(device!!)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("Connection state changed. Device at address (${device?.address}) has disconnected.")
                    connectedDevices.remove(device!!)
                }
                else -> { Timber.d("Connection state changed. Device at address (${device?.address}) state unknown.") }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            Timber.d("onCharacteristicWriteRequest:")

            if (value == null) return
            val receivedString = String(value, Charsets.UTF_8)
            if (receivedString.length < 5) { Timber.d("Unknown message."); return; }

            val code = receivedString.take(5)
            val message = receivedString.drop(5)
            when(code) {
                Constants.TEXT_MESSAGE -> dealWithTextMessage(message)
                Constants.CONNECTION_MESSAGE -> dealWithConnectionMessage(message)
                else -> dealWithOtherMessage(message)
            }

            Timber.d("Write characteristic value changed. Code: $code. Message: $message")
            passMessage(receivedString, device!!)
            if (responseNeeded) gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            Timber.d("Received descriptor write request.")
            if (responseNeeded) gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Timber.d("Mtu changed to: $mtu")
        }
    }

    //VALUES
    private val advertiser: Advertiser = Advertiser(bluetoothAdapter)
    private val connectedDevices: ArrayList<BluetoothDevice> = ArrayList()
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastConnectionMessage: MutableLiveData<String> = MutableLiveData()

    //VARIABLES
    private var gattServer: BluetoothGattServer? = null

    //FUNCTIONS
    override fun runBluetoothDevice(run: Boolean) = if (run) startServer() else stopServer()
    override fun getLastMessage(): LiveData<String> = lastMessage
    override fun getLastConnectionMessage(): LiveData<String> = lastConnectionMessage

    override fun sendMessage(message: String) {
        Timber.d("Sending message: $message.")
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.value = "${Constants.TEXT_MESSAGE}${Repository.username}:\n${message}".toByteArray(Charsets.UTF_8)
        for(device in connectedDevices) notifyDevice(device, characteristic)
    }

    override fun sendConnectionMessage(connected: Boolean) {
        TODO("Not implemented.")
    }

    private fun dealWithTextMessage(message: String): Int {
        lastMessage.postValue(message)
        return Constants.TEXT_MESSAGE_STATUS
    }

    private fun dealWithConnectionMessage(message: String): Int {
        lastConnectionMessage.postValue(message)
        return Constants.CONNECTION_MESSAGE_STATUS
    }

    private fun dealWithOtherMessage(message: String): Int {
        Timber.d("Received unknown message: $message")
        return Constants.OTHER_MESSAGE_STATUS
    }

    private fun passMessage(message: String, sender: BluetoothDevice) {
        val readCharacteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        readCharacteristic.value = message.toByteArray(Charsets.UTF_8)
        for (client in connectedDevices) if (client.address != sender.address) notifyDevice(client, readCharacteristic)
    }

    private fun startServer() {
        Timber.d("Starting server...")
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServer?.addService(createService()) ?: Timber.d("Gatt server is null.")
        advertiser.startAdvertising()
    }

    private fun stopServer() {
        Timber.d("Stopping server...")
        if (gattServer != null) {
            Timber.d("Server stopped.")
            advertiser.stopAdvertising()
            with(gattServer!!) {
                clearServices()
                close()
            }
            gattServer = null
        }
        else Timber.d("There is no need to stop server...")
    }

    private fun createService(): BluetoothGattService {
        Timber.d("Creating service...")
        return BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            .apply {
                addCharacteristic(createCharacteristicToRead())
                addCharacteristic(createCharacteristicToWrite())
            }
    }

    private fun createCharacteristicToRead(): BluetoothGattCharacteristic {
        Timber.d("Creating characteristic to read...")
        return BluetoothGattCharacteristic(
            Constants.READ_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        ).apply { addDescriptor(BluetoothGattDescriptor(Constants.DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)) }
    }

    private fun createCharacteristicToWrite(): BluetoothGattCharacteristic {
        Timber.d("Creating characteristic to write...")
        return BluetoothGattCharacteristic(
            Constants.WRITE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
    }

    private fun notifyDevice(client: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        gattServer!!.notifyCharacteristicChanged(client, characteristic, true)
    }
}