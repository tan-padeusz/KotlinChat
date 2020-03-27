package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.*
import android.content.Context
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber
import java.util.*

class Scanner(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val clientCallback : BluetoothGattCallback)
{
    //SCAN CALLBACK
    private val scanCallback : ScanCallback = object : ScanCallback()
    {
        private val innerTag = "scanCallback"

        override fun onScanResult(callbackType: Int, result: ScanResult?)
        {
            super.onScanResult(callbackType, result)
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$innerTag: $methodName: ")

            if(result?.scanRecord != null)
            {
                Timber.d("$innerTag: $methodName: name: ${result.scanRecord?.deviceName}")
                if(result.device != null)
                {
                    if(result.scanRecord?.serviceUuids != null)
                    {
                        for(uuid in result.scanRecord!!.serviceUuids)
                        {
                            if(checkUUIDEquality(uuid.uuid.toString(), Constants.serviceUUID.toString()))
                            {
                                Timber.d("$innerTag: $methodName: connected to: ${result.scanRecord?.deviceName}")
                                stopScanning()
                                if(!connected) connect(result.device)
                            }
                        }
                    }
                    else
                    {
                        Timber.d("$innerTag: $methodName: null uuid array: ")
                    }
                }
                else
                {
                    Timber.d("$innerTag: $methodName: null device: ")
                }
            }
            else
            {
                Timber.d("$innerTag: $methodName: empty scan record: ")
            }
        }
    }

    //VARIABLES
    private var connected : Boolean = false
    private var leScanner : BluetoothLeScanner? = null

    //FUNCTIONS
    private fun buildScanFilters() : LinkedList<ScanFilter>
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        val scanFilters : LinkedList<ScanFilter> = LinkedList()
        scanFilters.add(ScanFilter.Builder().build())
        return scanFilters
    }

    private fun buildScanSettings() : ScanSettings
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return ScanSettings.Builder().build()
    }

    private fun checkUUIDEquality(uuidOne : String, uuidTwo: String) : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(uuidOne.length != uuidTwo.length) { Timber.d("$methodName: wrong input data: "); return false; }
        for(n in 0..8) if(uuidOne[n] != uuidTwo[n]) return false
        return true
    }

    private fun connect(device : BluetoothDevice)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        device.connectGatt(context, false, clientCallback)
        stopScanning()
    }

    private fun createScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leScanner != null) { Timber.d("$methodName: scanner already created: "); return; }
        if(bluetoothAdapter.isEnabled) { Timber.d("$methodName: scanner created: "); leScanner = bluetoothAdapter.bluetoothLeScanner; }
        else { Timber.d("$methodName: bluetooth is not enabled: "); return; }
        if(leScanner == null) { Timber.d("$methodName: LE scan is not available: ") }
    }

    fun isConnected() : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return connected
    }

    private fun startScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leScanner != null)
        {
            Timber.d("$methodName: scanning started: ")
            leScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
        }
        else { Timber.d("$methodName: LE scan not available: ") }
    }

    fun startScanning()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        createScanner()
        startScanner()
    }

    private fun stopScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        if(leScanner != null)
        {
            Timber.d("$methodName: scanner stopped: ")
            leScanner?.stopScan(scanCallback)
        }
        else { Timber.d("$methodName: no need to stop scanner: ") }
    }

    fun stopScanning()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        stopScanner()
    }

    fun switchConnectionValue(newState : Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        connected = newState
    }

    //STATIC METHODS
    companion object
    {
        private var instance: Scanner? = null

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context, clientCallback : BluetoothGattCallback)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            instance = Scanner(bluetoothAdapter, context, clientCallback)
        }

        fun getInstance(): Scanner
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Timber.d("$methodName: ")

            return instance!!
        }
    }
}