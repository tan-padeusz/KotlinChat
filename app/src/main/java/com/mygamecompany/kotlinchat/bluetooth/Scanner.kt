package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.*
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mygamecompany.kotlinchat.utilities.*
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
            Log.d(logTag, "$innerTag: $methodName: ")

            if(result?.scanRecord != null)
            {
                Log.d(logTag, "$innerTag: $methodName: name: ${result.scanRecord?.deviceName}")
                if(result.device != null)
                {
                    if(result.scanRecord?.serviceUuids != null)
                    {
                        for(uuid in result.scanRecord!!.serviceUuids)
                        {
                            if(checkUUIDEquality(uuid.uuid.toString(), constants.serviceUUID.toString()))
                            {
                                Log.d(logTag, "$innerTag: $methodName: connected to: ${result.scanRecord?.deviceName}")
                                stopScanning()
                                if(!connected) connect(result.device)
                            }
                        }
                    }
                    else
                    {
                        Log.d(logTag, "$innerTag: $methodName: null uuid array: ")
                    }
                }
                else
                {
                    Log.d(logTag, "$innerTag: $methodName: null device: ")
                }
            }
            else
            {
                Log.d(logTag, "$innerTag: $methodName: empty scan record: ")
            }
        }
    }

    //CONSTANTS
    private val logTag: String = "KTC_${javaClass.simpleName}"
    private val constants: Constants = Constants.getInstance()

    //VARIABLES
    private var connected : Boolean = false
    private var leScanner : BluetoothLeScanner? = null

    //FUNCTIONS
    private fun buildScanFilters() : LinkedList<ScanFilter>
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val scanFilters : LinkedList<ScanFilter> = LinkedList()
        scanFilters.add(ScanFilter.Builder().build())
        return scanFilters
    }

    private fun buildScanSettings() : ScanSettings
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return ScanSettings.Builder().build()
    }

    private fun checkUUIDEquality(uuidOne : String, uuidTwo: String) : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(uuidOne.length != uuidTwo.length) { Log.d(logTag, "$methodName: wrong input data: "); return false; }
        for(n in 0..8) if(uuidOne[n] != uuidTwo[n]) return false
        return true
    }

    private fun connect(device : BluetoothDevice)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        device.connectGatt(context, false, clientCallback)
        stopScanning()
    }

    private fun createScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leScanner != null) { Log.d(logTag, "$methodName: scanner already created: "); return; }
        if(bluetoothAdapter.isEnabled) { Log.d(logTag, "$methodName: scanner created: "); leScanner = bluetoothAdapter.bluetoothLeScanner; }
        else { Log.d(logTag, "$methodName: bluetooth is not enabled: "); return; }
        if(leScanner == null) { Log.d(logTag, "$methodName: LE scan is not available: ") }
    }

    fun isConnected() : Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return connected
    }

    private fun startScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leScanner != null)
        {
            Log.d(logTag, "$methodName: scanning started: ")
            leScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
        }
        else { Log.d(logTag, "$methodName: LE scan not available: ") }
    }

    fun startScanning()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        createScanner()
        startScanner()
    }

    private fun stopScanner()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if(leScanner != null)
        {
            Log.d(logTag, "$methodName: scanner stopped: ")
            leScanner?.stopScan(scanCallback)
        }
        else { Log.d(logTag, "$methodName: no need to stop scanner: ") }
    }

    fun stopScanning()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        stopScanner()
    }

    fun switchConnectionValue(newState : Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        connected = newState
    }

    //STATIC METHODS
    companion object
    {
        private val logTag: String = "KTC_${Scanner::class.java.simpleName}"
        private var instance: Scanner? = null

        fun isNull(): Boolean
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return (instance == null)
        }

        fun createInstance(bluetoothAdapter : BluetoothAdapter, context : Context, clientCallback : BluetoothGattCallback)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            instance = Scanner(bluetoothAdapter, context, clientCallback)
        }

        fun getInstance(): Scanner
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return instance!!
        }
    }
}