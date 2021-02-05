package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.data.ConnectedDevice
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.interfaces.BLEDevice
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class BLEServer @Inject constructor (private val context : Context, private val advertiser: Advertiser): BLEDevice {
    //SERVER CALLBACK
    private val gattServerCallback : BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val method = this::onConnectionStateChange.name
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("$method: Connection state changed. Device at address (${device?.address}) has connected.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("$method: Connection state changed. Device at address (${device?.address}) has disconnected.")
                    setAndSendConnectionMessage(device!!, false)
                    connectedDevices.removeDevice(device)
                }
                else -> { Timber.d("$method: Connection state changed. Device at address (${device?.address}) state unknown.") }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val method = this::onCharacteristicWriteRequest.name
            Timber.d("$method:")

            if (value == null) return
            val receivedString = String(value, Charsets.UTF_8)
            if (receivedString.length < 5) { Timber.d("$method: Unknown message."); return }

            val code = receivedString.take(5)
            val message = receivedString.drop(5)
            when(code) {
                Constants.TEXT_MESSAGE -> dealWithTextMessage(message, device!!)
                Constants.USERNAME_MESSAGE -> dealWithUsernameMessage(message, device!!)
                else -> dealWithOtherMessage(code, message)
            }

            Timber.d("$method: Write characteristic value changed. Code: $code. Message: $message")
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
    private val connectedDevices: ArrayList<ConnectedDevice> = ArrayList()
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastConnectionMessage: MutableLiveData<String> = MutableLiveData()

    //VARIABLES
    private var gattServer: BluetoothGattServer? = null

    //FUNCTIONS
    override fun run() {
        startServer()
    }

    override fun stop() {
        stopServer()
    }

    override fun getLastMessage(): LiveData<String>{
        return lastMessage
    }

    override fun getLastConnectionMessage(): LiveData<String> {
        return lastConnectionMessage
    }

    override fun sendMessage(message: String) {
        Timber.d("Sending message: $message.")
        var characteristic: BluetoothGattCharacteristic? = null
        try {
            characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        } catch (ex: KotlinNullPointerException) {
            if (gattServer == null) Timber.d("Null gatt server @ sendMessage")
            else if (gattServer!!.getService(Constants.SERVICE_UUID) == null) Timber.d("Null service @ sendMessage")
            else Timber.d("Null characteristic @ sendMessage")
        }
        characteristic!!.value = (Constants.TEXT_MESSAGE + Repository.username + ":\n" + message).toByteArray(Charsets.UTF_8)
        for(client in connectedDevices) notifyDevice(client.device, characteristic)
    }

    private fun dealWithTextMessage(message: String, sender: BluetoothDevice) {
        lastMessage.postValue(message)
        passMessage(message, sender)
    }

    private fun dealWithUsernameMessage(username: String, sender: BluetoothDevice) {
        if (!connectedDevices.containsDevice(sender)) connectedDevices.add(ConnectedDevice(username, sender))
        respondWithUsername(sender)
        setAndSendConnectionMessage(sender, true)
    }

    private fun dealWithOtherMessage(code: String, message: String) {
        Timber.d("Received other message ($code): $message.")
    }

    private fun passMessage(message: String, sender: BluetoothDevice) {
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.value = (Constants.TEXT_MESSAGE + message).toByteArray(Charsets.UTF_8)
        for (client in connectedDevices) if (client.device != sender) notifyDevice(client.device, characteristic)
    }

    private fun respondWithUsername(sender: BluetoothDevice) {
        Timber.d("Responding with own username...")
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.value = (Constants.CONNECTION_MESSAGE + " CONNECTED TO " + Repository.username).toByteArray(Charsets.UTF_8)
        notifyDevice(sender, characteristic)
    }

    private fun setAndSendConnectionMessage(device: BluetoothDevice, connected: Boolean) {
        val username = connectedDevices.getUsernameByDevice(device)
        val message = if (connected) "$username HAS CONNECTED" else "$username HAS DISCONNECTED"
        lastConnectionMessage.postValue(message)
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.value = (Constants.CONNECTION_MESSAGE + message).toByteArray(Charsets.UTF_8)
        for (client in connectedDevices) if (client.device != device) notifyDevice(client.device, characteristic)
    }

    fun forceDisconnect() {
        val characteristic = gattServer!!.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.READ_CHARACTERISTIC_UUID)
        characteristic.value = Constants.FORCE_DISCONNECTION_MESSAGE.toByteArray(Charsets.UTF_8)
        for (client in connectedDevices) notifyDevice(client.device, characteristic)
    }

    private fun notifyDevice(client: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        Timber.d("Notify device result: ${gattServer!!.notifyCharacteristicChanged(client, characteristic, true)}")
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
            gattServer!!.clearServices()
            gattServer!!.close()
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

    fun dummyMethod() {  }
}