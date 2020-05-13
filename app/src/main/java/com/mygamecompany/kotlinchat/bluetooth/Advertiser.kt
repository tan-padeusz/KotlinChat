package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import com.mygamecompany.kotlinchat.data.Repository.TAG
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

class Advertiser(private val bluetoothAdapter: BluetoothAdapter, private val context: Context, private val serverCallback: BluetoothGattServerCallback) {

    //ADVERTISE CALLBACK
    private val advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {

    }

    //VARIABLES
    private var gattServer: BluetoothGattServer? = null
    private var leAdvertiser: BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseData(): AdvertiseData {
        Timber.d("$TAG: buildAdvertiseData:")
        val builder: AdvertiseData.Builder = AdvertiseData.Builder()
        builder.addServiceUuid(Constants.parcelServiceUUID)
        return builder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        Timber.d("$TAG: buildAdvertiseSettings:")
        return AdvertiseSettings.Builder().build()
    }

    private fun createAdvertiser() {
        Timber.d("$TAG: createAdvertiser:")
        if (bluetoothAdapter.isEnabled) {
            Timber.d("$TAG: createAdvertiser: advertiser created:")
            leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        }
        if (leAdvertiser == null) Timber.d("$TAG: createAdvertiser: LE advertising not available: ")
    }

    private fun createCharacteristic(): BluetoothGattCharacteristic {
        Timber.d("$TAG: createCharacteristic:")
        val characteristic = BluetoothGattCharacteristic(Constants.characteristicUUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.addDescriptor(BluetoothGattDescriptor(Constants.descriptorUUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        return characteristic
    }

    private fun createService(): BluetoothGattService {
        Timber.d("$TAG: createService:")
        val service = BluetoothGattService(Constants.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        if (!service.addCharacteristic(createCharacteristic())) Timber.d("$TAG: createService: server characteristic is null:")
        return service
    }

    private fun startAdvertiser() {
        Timber.d("$TAG: startAdvertiser:")
        if (leAdvertiser != null) {
            Timber.d("$TAG: startAdvertiser: advertiser started:")
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback )
        }
        else {
            Timber.d("$TAG: startAdvertiser: advertiser not created:")
        }
    }

    private fun stopAdvertiser() {
        Timber.d("$TAG: stopAdvertiser:")
        if (leAdvertiser != null) {
            Timber.d("$TAG: stopAdvertiser: advertiser stopped:")
            leAdvertiser?.stopAdvertising(advertiseCallback)
        } else { Timber.d("$TAG: stopAdvertiser: no need to stop advertiser:") }
    }

    private fun startServer() {
        Timber.d("$TAG: startServer:")
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        gattServer?.addService(createService()) ?: Timber.d("$TAG: startServer: gatt server is null: ")
    }

    private fun stopServer() {
        Timber.d("$TAG: stopServer:")
        if (gattServer != null) {
            Timber.d("$TAG: stopServer: server stopped: ")
            gattServer?.clearServices()
            gattServer?.close()
            gattServer = null
        }
        else { Timber.d("$TAG: stopServer: no need to stop server: ") }
    }

    fun startAdvertising() {
        Timber.d("$TAG: startAdvertising:")
        if (leAdvertiser == null) createAdvertiser()
        startServer()
        startAdvertiser()
    }

    fun stopAdvertising() {
        Timber.d("$TAG: stopAdvertising:")
        stopAdvertiser()
        stopServer()
    }

    fun getGattServer(): BluetoothGattServer? {
        Timber.d("$TAG: getGattServer:")
        return gattServer
    }

    fun getServerCharacteristic(): BluetoothGattCharacteristic {
        Timber.d("$TAG: getServerCharacteristic:")
        return gattServer!!.getService(Constants.serviceUUID) .getCharacteristic(Constants.characteristicUUID)
    }
}