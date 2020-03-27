package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import org.greenrobot.eventbus.EventBus
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Client(bluetoothAdapter : BluetoothAdapter, context : Context)
{
    //CLIENT CALLBACK
    private val gattClientCallback : BluetoothGattCallback = object : BluetoothGattCallback()
    {
        private val innerTag = "gattClientCallback"

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
        {
            super.onCharacteristicChanged(gatt, characteristic)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            val text = String(characteristic!!.value)
            when(text[0])
            {
                //TODO("Should be erased?")
                //Constants.ping -> { Timber.d("$innerTag: $methodName: PING: ") }
                Constants.message ->
                {
                    Timber.d("$innerTag: $methodName: MESSAGE: ")
                    var newText = ""
                    for(i in 1 until text.length) newText += text[i]
                    EventBus.getDefault().post(Events.SetMessage(newText))
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int)
        {
            super.onConnectionStateChange(gatt, status, newState)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            when (newState)
            {
                BluetoothProfile.STATE_CONNECTED ->
                {
                    Timber.d("$innerTag: $methodName: SATE_CONNECTED: ")

                    Scanner.getInstance().switchConnectionValue(true)
                    EventBus.getDefault().post(Events.ConnectionMessage(gatt?.device!!.address, true))
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED ->
                {
                    Timber.d("$innerTag: $methodName: STATE_DISCONNECTED: ")

                    Scanner.getInstance().switchConnectionValue(false)
                    clientCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    EventBus.getDefault().post(Events.ConnectionMessage(gatt?.device!!.address, false))
                    gatt.close()
                }
                else -> { Timber.d("$innerTag: $methodName: STATE_UNKNOWN: ") }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int)
        {
            super.onMtuChanged(gatt, mtu, status)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            enableIndication(gatt as BluetoothGatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
        {
            super.onServicesDiscovered(gatt, status)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            when (status)
            {
                BluetoothGatt.GATT_SUCCESS ->
                {
                    Timber.d("$innerTag: $methodName: discovery success: ")
                    clientGatt = gatt
                    clientCharacteristic = clientGatt?.getService(Constants.serviceUUID)?.getCharacteristic(Constants.characteristicUUID)
                    if(clientCharacteristic == null) { Timber.d("$innerTag: $methodName: discovery success: client characteristic is null") }
                    gatt!!.requestMtu(512)
                }
                else -> { Timber.d("$innerTag: $methodName: discovery failure: ") }
            }
        }
    }

    //INITIALISE SCANNER
    init
    {
        Scanner.createInstance(bluetoothAdapter, context, gattClientCallback)
    }

    //VARIABLES
    private var clientCharacteristic : BluetoothGattCharacteristic? = null
    private var clientGatt : BluetoothGatt? = null

    //FUNCTIONS
    private fun enableIndication(gatt : BluetoothGatt)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val characteristic : BluetoothGattCharacteristic = gatt.getService(Constants.serviceUUID).getCharacteristic(Constants.characteristicUUID)
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor : BluetoothGattDescriptor = characteristic.getDescriptor(Constants.descriptorUUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Timber.d("$methodName: result: ${gatt.writeDescriptor(descriptor)}")
    }

    fun enableScan(enable : Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: enable: $enable")

        if(enable) Scanner.getInstance().startScanning()
        else Scanner.getInstance().stopScanning()
    }

    fun isConnected() : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return Scanner.getInstance().isConnected()
    }

    fun sendMessageToServer(name : String, message : String)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(clientCharacteristic == null) { Timber.d("$methodName: clientCharacteristic is null: ") }
        else
        {
            clientCharacteristic!!.setValue("${Constants.message}${name}:\n${message}")
            //TODO("Is it necessary?")
            //clientCharacteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            Timber.d("$methodName: result: ${clientGatt?.writeCharacteristic(clientCharacteristic)}")
        }
    }

    //STATIC METHODS
    companion object
    {
        private var instance: Client? = null

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            instance = Client(bluetoothAdapter, context)
        }

        fun getInstance(): Client
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            return instance!!
        }
    }
}