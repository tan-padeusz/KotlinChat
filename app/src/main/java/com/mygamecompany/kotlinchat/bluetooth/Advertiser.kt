package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

class Advertiser(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val serverCallback : BluetoothGattServerCallback)
{
    //ADVERTISE CALLBACK
    private val advertiseCallback  : AdvertiseCallback = object : AdvertiseCallback()
    {

    }

    //VARIABLES
    private var gattServer : BluetoothGattServer? = null
    private var leAdvertiser : BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseData() : AdvertiseData
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val builder : AdvertiseData.Builder = AdvertiseData.Builder()
            .addServiceUuid(Constants.parcelServiceUUID)
            .setIncludeDeviceName(true)
        return builder.build()
    }

    private fun buildAdvertiseSettings() : AdvertiseSettings
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return AdvertiseSettings.Builder().build()
    }

    private fun createAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leAdvertiser != null) { Timber.d("$methodName: advertiser already created: "); return; }
        if(bluetoothAdapter.isEnabled) { Timber.d("$methodName: advertiser created: "); leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser; }
        if(leAdvertiser == null) { Timber.d("$methodName: LE advertising not available: ") }
    }

    private fun createCharacteristic() : BluetoothGattCharacteristic
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val characteristic = BluetoothGattCharacteristic(Constants.characteristicUUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        characteristic.addDescriptor(BluetoothGattDescriptor(Constants.descriptorUUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        return characteristic
    }

    private fun createService() : BluetoothGattService
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val service = BluetoothGattService(Constants.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        if (!service.addCharacteristic(createCharacteristic())) Timber.d("$methodName: server characteristic is null: ")
        return service
    }

    fun getGattServer() : BluetoothGattServer?
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return gattServer
    }

    fun getServerCharacteristic() : BluetoothGattCharacteristic
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return gattServer!!.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
    }

    private fun startAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leAdvertiser != null)
        {
            Timber.d("$methodName: advertiser started: ")
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback)
        }
        else { Timber.d("$methodName: advertiser not created: ") }
    }

    fun startAdvertising()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        createAdvertiser()
        startServer()
        startAdvertiser()
    }

    private fun startServer()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val bluetoothManager : BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        gattServer?.addService(createService()) ?: Timber.d("$methodName: gatt server is null: ")
    }

    private fun stopAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leAdvertiser != null)
        {
            Timber.d("$methodName: advertiser stopped: ")
            leAdvertiser?.stopAdvertising(advertiseCallback)
        }
        else { Timber.d("$methodName: no need to stop advertiser: ") }
    }

    fun stopAdvertising()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        stopAdvertiser()
        stopServer()
    }

    private fun stopServer()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(gattServer != null)
        {
            Timber.d("$methodName: server stopped: ")
            gattServer?.clearServices()
            gattServer?.close()
            gattServer = null
        }
        else { Timber.d("$methodName: no need to stop server: ") }
    }

    //STATIC METHODS
    companion object
    {
        private var instance: Advertiser? = null

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context, serverCallback : BluetoothGattServerCallback)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            instance = Advertiser(bluetoothAdapter, context, serverCallback)
        }

        fun getInstance(): Advertiser
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            return instance!!
        }
    }
}