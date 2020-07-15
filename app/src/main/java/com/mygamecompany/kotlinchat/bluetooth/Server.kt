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

class Server(bluetoothAdapter : BluetoothAdapter, private val context : Context): ChatDevice {

    //SERVER CALLBACK
    private val gattServerCallback : BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("Connection state changed. New state: STATE_CONNECTED.")
                    if(!connectedDevices.contains(device)) connectedDevices.add(device!!)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("Connection state changed. New state: STATE_DISCONNECTED.")
                    connectedDevices.remove(device!!)
                }
                else -> { Timber.d("Connection state changed. New state: STATE_UNKNOWN.") }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Timber.d("Received characteristic read request.")
            gattServer!!.sendResponse(device, requestId, 1, offset, byteArrayOf(1))
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            Timber.d("Received descriptor read request.")
            gattServer!!.sendResponse(device, requestId, 1, offset, byteArrayOf(1))
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            Timber.d("Received characteristic write request.")
            val message = String(value!!)
            val status: Int = when(message[0]) {
                Constants.TEXT_MESSAGE_RECEIVER -> Constants.TEXT_MESSAGE_STATUS
                Constants.CONNECTION_MESSAGE -> Constants.CONNECTION_MESSAGE_STATUS
                Constants.DISCONNECTION_MESSAGE -> Constants.DISCONNECTION_MESSAGE_STATUS
                else -> Constants.OTHER_MESSAGE_STATUS
            }
            Timber.d("Write characteristic value changed. Status: $status. Message: ${message.removeRange(0, 0)}")
            lastMessage.postValue(message.removeRange(0, 0))
            val readCharacteristic: BluetoothGattCharacteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
            readCharacteristic.setValue(message)
            for (client in connectedDevices) if (client.address != device?.address) notifyDevice(client, readCharacteristic)
            if(responseNeeded) gattServer!!.sendResponse(device, requestId, status, offset, byteArrayOf(1))
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Timber.d("Mtu changed to: $mtu")
        }
    }

    //CONSTANTS
    private val advertiser: Advertiser = Advertiser(bluetoothAdapter)
    private val connectedDevices: LinkedList<BluetoothDevice> = LinkedList()
    private val lastMessage: MutableLiveData<String> = MutableLiveData()

    //VARIABLES
    private var gattServer: BluetoothGattServer? = null

    //FUNCTIONS
    override fun runBluetoothDevice(run: Boolean) = if (run) startServer() else stopServer()
    fun enableAdvertising(enable: Boolean) = if (enable) advertiser.startAdvertising() else advertiser.stopAdvertising()

    override fun sendMessage(message: String) {
        Timber.d("Sending message: $message.")
        val fullMessage = "${Repository.username}:\n${message}"
        lastMessage.postValue(Constants.TEXT_MESSAGE_SENDER + fullMessage)
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.setValue(Constants.TEXT_MESSAGE_RECEIVER + fullMessage)
        for(device in connectedDevices) notifyDevice(device, characteristic)
    }

    override fun receiveMessage(): LiveData<String> {
        return lastMessage
    }

    private fun startServer() {
        Timber.d("Starting server...")
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServer?.addService(createService()) ?: Timber.d("Gatt server is null.")
    }

    private fun stopServer() {
        Timber.d("Stopping server...")
        if (gattServer != null) {
            Timber.d("Server stopped.")
            with(gattServer!!) {
                clearServices()
                close()
            }
            gattServer = null
        }
        else Timber.d("There is no need to stop server...")
    }

    private fun notifyDevice(client: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        gattServer!!.notifyCharacteristicChanged(client, characteristic, true)
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
}