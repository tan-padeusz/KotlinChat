package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

class Advertiser(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val serverCallback : BluetoothGattServerCallback) {

    //ADVERTISE CALLBACK
    private val advertiseCallback  : AdvertiseCallback = object : AdvertiseCallback() {

    }

    //VARIABLES
    private var gattServer : BluetoothGattServer? = null
    private var leAdvertiser : BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseData() : AdvertiseData {
        Timber.d("")
        val builder: AdvertiseData.Builder = AdvertiseData.Builder()
            .addServiceUuid(Constants.parcelServiceUUID)
        return builder.build()
    }

    private fun buildAdvertiseSettings() : AdvertiseSettings {
        Timber.d("")
        return AdvertiseSettings.Builder().build()
    }

    private fun createAdvertiser() {
        Timber.d("")
        if(bluetoothAdapter.isEnabled) { Timber.d("advertiser created: "); leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser; }
        if(leAdvertiser == null) { Timber.d("LE advertising not available: ") }
    }

    private fun createCharacteristic() : BluetoothGattCharacteristic {
        val characteristic = BluetoothGattCharacteristic(Constants.characteristicUUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.addDescriptor(BluetoothGattDescriptor(Constants.descriptorUUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        return characteristic
    }

    private fun createService() : BluetoothGattService {
        val service = BluetoothGattService(Constants.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        if (!service.addCharacteristic(createCharacteristic())) Timber.d("server characteristic is null: ")
        return service
    }

    private fun startAdvertiser() {
        Timber.d("")
        if(leAdvertiser != null) {
            Timber.d("advertiser started: ")
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback)
        }
        else { Timber.d("advertiser not created: ") }
    }

    private fun stopAdvertiser() {
        Timber.d("")
        if(leAdvertiser != null) {
            Timber.d("advertiser stopped: ")
            leAdvertiser?.stopAdvertising(advertiseCallback)
        }
        else { Timber.d("no need to stop advertiser: ") }
    }

    private fun startServer() {
        Timber.d("")
        val bluetoothManager : BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        gattServer?.addService(createService()) ?: Timber.d("gatt server is null: ")
    }

    private fun stopServer() {
        Timber.d("")
        if(gattServer != null) {
            Timber.d("server stopped: ")
            gattServer?.clearServices()
            gattServer?.close()
            gattServer = null
        }
        else { Timber.d("no need to stop server: ") }
    }

    fun startAdvertising() {
        Timber.d("")
        if (leAdvertiser == null) createAdvertiser()
        startServer()
        startAdvertiser()
    }

    fun stopAdvertising()  {
        Timber.d("")
        stopAdvertiser()
        stopServer()
    }

    fun getGattServer() : BluetoothGattServer? {
        Timber.d("")
        return gattServer
    }

    fun getServerCharacteristic() : BluetoothGattCharacteristic {
        Timber.d("")
        return gattServer!!.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
    }
}