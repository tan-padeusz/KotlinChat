package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

class Advertiser(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val serverCallback : BluetoothGattServerCallback) {

    //ADVERTISE CALLBACK
    private val advertiseCallback  : AdvertiseCallback = object : AdvertiseCallback() {

    }

    //VALUES
    private val appTag: String = Repository.TAG

    //VARIABLES
    private var gattServer: BluetoothGattServer? = null
    private var leAdvertiser: BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseData() : AdvertiseData {
        Timber.d(appTag)
        val builder: AdvertiseData.Builder = AdvertiseData.Builder()
        builder.addServiceUuid(Constants.parcelServiceUUID)
        return builder.build()
    }

    private fun buildAdvertiseSettings() : AdvertiseSettings {
        Timber.d(appTag)
        return AdvertiseSettings.Builder().build()
    }

    private fun createAdvertiser() {
        Timber.d(appTag)
        if(bluetoothAdapter.isEnabled) {
            Timber.d("$appTag: advertiser created: ")
            leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        }
        if(leAdvertiser == null) Timber.d("$appTag: LE advertising not available: ")
    }

    private fun createCharacteristic() : BluetoothGattCharacteristic {
        Timber.d(appTag)
        val characteristic = BluetoothGattCharacteristic(Constants.characteristicUUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.addDescriptor(BluetoothGattDescriptor(Constants.descriptorUUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        return characteristic
    }

    private fun createService() : BluetoothGattService {
        Timber.d(appTag)
        val service = BluetoothGattService(Constants.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        if (!service.addCharacteristic(createCharacteristic())) Timber.d("$appTag: server characteristic is null: ")
        return service
    }

    private fun startAdvertiser() {
        Timber.d(appTag)
        if(leAdvertiser != null) {
            Timber.d("$appTag: advertiser started: ")
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback)
        }
        else { Timber.d("$appTag: advertiser not created: ") }
    }

    private fun stopAdvertiser() {
        Timber.d(appTag)
        if(leAdvertiser != null) {
            Timber.d("$appTag: advertiser stopped: ")
            leAdvertiser?.stopAdvertising(advertiseCallback)
        }
        else { Timber.d("$appTag: no need to stop advertiser: ") }
    }

    private fun startServer() {
        Timber.d(appTag)
        val bluetoothManager : BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        gattServer?.addService(createService()) ?: Timber.d("$appTag: gatt server is null: ")
    }

    private fun stopServer() {
        Timber.d(appTag)
        if(gattServer != null) {
            Timber.d("$appTag: server stopped: ")
            gattServer?.clearServices()
            gattServer?.close()
            gattServer = null
        }
        else { Timber.d("$appTag: no need to stop server: ") }
    }

    fun startAdvertising() {
        Timber.d(appTag)
        if (leAdvertiser == null) createAdvertiser()
        startServer()
        startAdvertiser()
    }

    fun stopAdvertising()  {
        Timber.d(appTag)
        stopAdvertiser()
        stopServer()
    }

    fun getGattServer() : BluetoothGattServer? {
        Timber.d(appTag)
        return gattServer
    }

    fun getServerCharacteristic() : BluetoothGattCharacteristic {
        Timber.d(appTag)
        return gattServer!!.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
    }
}