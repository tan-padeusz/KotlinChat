package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.util.Log
import com.mygamecompany.kotlinchat.utilities.Constants

class Advertiser(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val serverCallback : BluetoothGattServerCallback)
{
    //ADVERTISE CALLBACK
    private val advertiseCallback  : AdvertiseCallback = object : AdvertiseCallback()
    {

    }

    //CONSTANTS
    private val logTag: String = "KTC_${javaClass.simpleName}"
    private val constants: Constants = Constants.getInstance()

    //VARIABLES
    private var gattServer : BluetoothGattServer? = null
    private var leAdvertiser : BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseData() : AdvertiseData
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val builder : AdvertiseData.Builder = AdvertiseData.Builder()
            .addServiceUuid(constants.parcelServiceUUID)
            .setIncludeDeviceName(true)
        return builder.build()
    }

    private fun buildAdvertiseSettings() : AdvertiseSettings
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return AdvertiseSettings.Builder().build()
    }

    private fun createAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leAdvertiser != null) { Log.d(logTag, "$methodName: advertiser already created: "); return; }
        if(bluetoothAdapter.isEnabled) { Log.d(logTag, "$methodName: advertiser created: "); leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser; }
        if(leAdvertiser == null) { Log.d(logTag, "$methodName: LE advertising not available: ") }
    }

    private fun createCharacteristic() : BluetoothGattCharacteristic
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val characteristic = BluetoothGattCharacteristic(constants.characteristicUUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        characteristic.addDescriptor(BluetoothGattDescriptor(constants.descriptorUUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        return characteristic
    }

    private fun createService() : BluetoothGattService
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val service = BluetoothGattService(constants.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        if (!service.addCharacteristic(createCharacteristic())) Log.d(logTag, "$methodName: server characteristic is null: ")
        return service
    }

    fun getGattServer() : BluetoothGattServer?
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return gattServer
    }

    fun getServerCharacteristic() : BluetoothGattCharacteristic
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return gattServer!!.getService(constants.serviceUUID).getCharacteristic(constants.characteristicUUID)
    }

    private fun startAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leAdvertiser != null)
        {
            Log.d(logTag, "$methodName: advertiser started: ")
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback)
        }
        else { Log.d(logTag, "$methodName: advertiser not created: ") }
    }

    fun startAdvertising()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        createAdvertiser()
        startServer()
        startAdvertiser()
    }

    private fun startServer()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val bluetoothManager : BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        gattServer?.addService(createService()) ?: Log.d(logTag, "$methodName: gatt server is null: ")
    }

    private fun stopAdvertiser()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leAdvertiser != null)
        {
            Log.d(logTag, "$methodName: advertiser stopped: ")
            leAdvertiser?.stopAdvertising(advertiseCallback)
        }
        else { Log.d(logTag, "$methodName: no need to stop advertiser: ") }
    }

    fun stopAdvertising()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        stopAdvertiser()
        stopServer()
    }

    private fun stopServer()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(gattServer != null)
        {
            Log.d(logTag, "$methodName: server stopped: ")
            gattServer?.clearServices()
            gattServer?.close()
            gattServer = null
        }
        else { Log.d(logTag, "$methodName: no need to stop server: ") }
    }

    //STATIC METHODS
    companion object
    {
        private val logTag: String = "KTC_${Advertiser::class.java.simpleName}"
        private var instance: Advertiser? = null

        fun isNull(): Boolean
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return (instance == null)
        }

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context, serverCallback : BluetoothGattServerCallback)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            instance = Advertiser(bluetoothAdapter, context, serverCallback)
        }

        fun getInstance(): Advertiser
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return instance!!
        }
    }
}