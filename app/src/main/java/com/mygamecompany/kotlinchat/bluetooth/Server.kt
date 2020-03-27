package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.mygamecompany.kotlinchat.utilities.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
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
            Timber.d("$innerTag: $methodName: ")

            when (newState)
            {
                BluetoothProfile.STATE_CONNECTED ->
                {
                    Timber.d("$innerTag: $methodName: STATE_CONNECTED: ")

                    EventBus.getDefault().post(Events.ConnectionMessage(device?.address ?: "UNKNOWN_ADDRESS", true))
                    if(!connectedDevices.contains(device)) connectedDevices.add(device!!)
                }
                BluetoothProfile.STATE_DISCONNECTED ->
                {
                    Timber.d("$innerTag: $methodName: STATE_DISCONNECTED: ")

                    EventBus.getDefault().post(Events.ConnectionMessage(device?.address ?: "UNKNOWN_ADDRESS", false))
                    if(connectedDevices.contains(device)) connectedDevices.remove(device)
                }
                else -> { Timber.d("$innerTag: $methodName: STATE_UNKNOWN: ") }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?)
        {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            val text = String(value as ByteArray)
            var status = Constants.messageNotReceived
            if(text[0] == Constants.message)
            {
                Timber.d("$innerTag: $methodName: MESSAGE: ")

                Advertiser.getInstance().getServerCharacteristic().setValue(text)
                for(client in connectedDevices) if(client.address != device?.address) notifyDevice(client)
                var newText = ""
                for(i in 1 until text.length) newText += text[i]
                EventBus.getDefault().post(Events.SetMessage(newText))
                status = Constants.messageReceived
            }
            if(responseNeeded) Advertiser.getInstance().getGattServer()!!.sendResponse(device, requestId, status, offset, byteArrayOf('0'.toByte()))
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?)
        {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            Advertiser.getInstance().getGattServer()?.sendResponse(device, requestId, 0, offset, value) ?: Timber.d("$innerTag: $methodName: advertiser gattServer is null: ")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int)
        {
            super.onMtuChanged(device, mtu)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: $mtu")
        }
    }

    //INITIALISE ADVERTISER
    init
    {
        Advertiser.createInstance(bluetoothAdapter, context, gattServerCallback)
    }

    //CONSTANTS
    private val connectedDevices : LinkedList<BluetoothDevice> = LinkedList()

    //VARIABLES
    //private var timer : Timer? = null

    //FUNCTIONS
    fun enableAdvertisement(enable : Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

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
        Timber.d("$methodName: ")

        return connectedDevices.count() != 0
    }

    private fun notifyDevice(client: BluetoothDevice)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        Advertiser.getInstance().getGattServer()!!.notifyCharacteristicChanged(client, Advertiser.getInstance().getServerCharacteristic(), false)
    }

    fun sendMessageToClient(name : String, message : String)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: $message")

        val characteristic = Advertiser.getInstance().getServerCharacteristic()
        characteristic.setValue("${Constants.message}${name}:\n${message}")
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        for(device in connectedDevices) notifyDevice(device)
    }

    //TODO("Should be erased?")
    /*private fun setTimerSettings() : Timer
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val timer = Timer("timer", false)
        timer.schedule(object : TimerTask()
        {
            override fun run()
            {
                Timber.d("$methodName: sending PING: ")
                Advertiser.getInstance().getServerCharacteristic().setValue(Constants.ping.toString())
                for (device in connectedDevices) notifyDevice(device)
            }
        }, 20000, 20000)
        return timer
    }*/

    //STATIC METHODS
    companion object
    {
        private var instance: Server? = null

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            instance = Server(bluetoothAdapter, context)
        }

        fun getInstance(): Server
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            return instance!!
        }
    }
}