package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.mygamecompany.kotlinchat.utilities.*
import org.greenrobot.eventbus.EventBus
import java.util.*

class Server(bluetoothAdapter : BluetoothAdapter, context : Context)
{
    //SERVER CALLBACK
    private val gattServerCallback : BluetoothGattServerCallback = object : BluetoothGattServerCallback()
    {
        private val innerTag = "gattServerCallback"

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int)
        {
            super.onConnectionStateChange(device, status, newState)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$innerTag: $methodName: ")

            when (newState)
            {
                BluetoothProfile.STATE_CONNECTED ->
                {
                    Log.d(logTag, "$innerTag: $methodName: STATE_CONNECTED: ")

                    EventBus.getDefault().post(Events.ConnectionMessage(device?.address ?: "UNKNOWN_ADDRESS", true))
                    if(!connectedDevices.contains(device)) connectedDevices.add(device!!)
                }
                BluetoothProfile.STATE_DISCONNECTED ->
                {
                    Log.d(logTag, "$innerTag: $methodName: STATE_DISCONNECTED: ")

                    EventBus.getDefault().post(Events.ConnectionMessage(device?.address ?: "UNKNOWN_ADDRESS", false))
                    if(connectedDevices.contains(device)) connectedDevices.remove(device)
                }
                else -> { Log.d(logTag, "$innerTag: $methodName: STATE_UNKNOWN: ") }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?)
        {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$innerTag: $methodName: ")

            val text = String(value as ByteArray)
            var status = constants.messageNotReceived
            if(text[0] == constants.message)
            {
                Log.d(logTag, "$innerTag: $methodName: MESSAGE: ")

                Advertiser.getInstance().getServerCharacteristic().setValue(text)
                for(client in connectedDevices) if(client.address != device?.address) notifyDevice(client)
                var newText = ""
                for(i in 1 until text.length) newText += text[i]
                EventBus.getDefault().post(Events.SetMessage(newText))
                status = constants.messageReceived
            }
            if(responseNeeded) Advertiser.getInstance().getGattServer()!!.sendResponse(device, requestId, status, offset, byteArrayOf('0'.toByte()))
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?)
        {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$innerTag: $methodName: ")

            Advertiser.getInstance().getGattServer()?.sendResponse(device, requestId, 0, offset, value) ?: Log.d(logTag, "$innerTag: $methodName: advertiser gattServer is null: ")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int)
        {
            super.onMtuChanged(device, mtu)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$innerTag: $methodName: $mtu")
        }
    }

    //INITIALISE ADVERTISER
    init
    {
        Advertiser.createInstance(bluetoothAdapter, context, gattServerCallback)
    }

    //CONSTANTS
    private val connectedDevices : LinkedList<BluetoothDevice> = LinkedList()
    private val constants: Constants = Constants.getInstance()
    private val logTag: String = "KTC_${javaClass.simpleName}"

    //VARIABLES
    //private var timer : Timer? = null

    //FUNCTIONS
    fun enableAdvertisement(enable : Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        //TODO("Should be erased?")
        //if (timer == null) timer = setTimerSettings()
        when(enable)
        {
            true -> Advertiser.getInstance().startAdvertising()
            false -> Advertiser.getInstance().stopAdvertising()
        }
    }

    fun hasConnectedDevices() : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return connectedDevices.count() != 0
    }

    private fun notifyDevice(client: BluetoothDevice)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        Advertiser.getInstance().getGattServer()!!.notifyCharacteristicChanged(client, Advertiser.getInstance().getServerCharacteristic(), false)
    }

    fun sendMessageToClient(name : String, message : String)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: $message")

        val characteristic = Advertiser.getInstance().getServerCharacteristic()
        characteristic.setValue("${constants.message}${name}:\n${message}")
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        for(device in connectedDevices) notifyDevice(device)
    }

    //TODO("Should be erased?")
    /*private fun setTimerSettings() : Timer
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val timer = Timer("timer", false)
        timer.schedule(object : TimerTask()
        {
            override fun run()
            {
                Log.d(logTag, "$methodName: sending PING: ")
                Advertiser.getInstance().getServerCharacteristic().setValue(constants.ping.toString())
                for (device in connectedDevices) notifyDevice(device)
            }
        }, 20000, 20000)
        return timer
    }*/

    //STATIC METHODS
    companion object
    {
        private val logTag: String = "KTC_${Server::class.java.simpleName}"
        private var instance: Server? = null

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            instance = Server(bluetoothAdapter, context)
        }

        fun getInstance(): Server
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return instance!!
        }
    }
}