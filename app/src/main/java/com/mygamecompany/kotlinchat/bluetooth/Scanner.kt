package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.*
import android.content.Context
import com.mygamecompany.kotlinchat.utilities.*
import com.mygamecompany.kotlinchat.data.Repository.TAG
import timber.log.Timber
import java.util.*

class Scanner(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val clientCallback : BluetoothGattCallback) {
    //SCAN CALLBACK
    private val scanCallback : ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Timber.d("$TAG: scanCallback: onScanResult:")

            if(result?.scanRecord != null) {
                Timber.d("$TAG: scanCallback: onScanResult: name: ${result.scanRecord?.deviceName}")
                if(result.device != null) {
                    if(result.scanRecord?.serviceUuids != null) {
                        for(uuid in result.scanRecord!!.serviceUuids) {
                            if(checkUUIDEquality(uuid.uuid.toString(), Constants.serviceUUID.toString())) {
                                Timber.d("$TAG: scanCallback: onScanResult: connected to: ${result.scanRecord?.deviceName}")
                                stopScanning()
                                if(!connected) connect(result.device)
                            }
                        }
                    }
                    else {
                        Timber.d("$TAG: scanCallback: onScanResult: null uuid array: ")
                    }
                }
                else {
                    Timber.d("$TAG: scanCallback: onScanResult: null device: ")
                }
            }
            else {
                Timber.d("$TAG: scanCallback: onScanResult: empty scan record: ")
            }
        }
    }

    //VARIABLES
    private var connected : Boolean = false
    private var leScanner : BluetoothLeScanner? = null

    //FUNCTIONS
    private fun buildScanFilters() : LinkedList<ScanFilter> {
        Timber.d("$TAG: buildScanFilters:")
        val scanFilters : LinkedList<ScanFilter> = LinkedList()
        scanFilters.add(ScanFilter.Builder().build())
        return scanFilters
    }

    private fun buildScanSettings() : ScanSettings {
        Timber.d("$TAG: buildScanSettings:")
        return ScanSettings.Builder().build()
    }

    private fun checkUUIDEquality(uuidOne : String, uuidTwo: String) : Boolean {
        Timber.d("$TAG: checkUUIDEquality:")
        if(uuidOne.length != uuidTwo.length) {
            Timber.d("$TAG: checkUUIDEquality wrong input data:")
            return false
        }
        for(n in 0..8) if(uuidOne[n] != uuidTwo[n]) return false
        return true
    }

    private fun connect(device : BluetoothDevice) {
        Timber.d("$TAG: connect:")
        device.connectGatt(context, false, clientCallback)
        stopScanning()
    }

    private fun createScanner() {
        Timber.d("$TAG: createScanner:")
        if(leScanner != null) { Timber.d("$TAG: createScanner: scanner already created:"); return; }
        if(bluetoothAdapter.isEnabled) {
            Timber.d("$TAG: createScanner: scanner created:")
            leScanner = bluetoothAdapter.bluetoothLeScanner
        }
        else {
            Timber.d("$TAG: createScanner: bluetooth is not enabled: ")
            return
        }
        if(leScanner == null) { Timber.d("$TAG: createScanner: LE scan is not available:") }
    }

    fun isConnected() : Boolean {
        Timber.d("$TAG: isConnected: connected=$connected")
        return connected
    }

    private fun startScanner() {
        Timber.d("$TAG: startScanner:")
        if(leScanner != null) {
            Timber.d("$TAG: startScanner: scanning started:")
            leScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
        }
        else { Timber.d("$TAG: startScanner: LE scan not available:") }
    }

    fun startScanning() {
        Timber.d("$TAG: startScanning:")
        createScanner()
        startScanner()
    }

    private fun stopScanner() {
        Timber.d("$TAG: stopScanner:")
        if(leScanner != null) {
            Timber.d("$TAG: stopScanner: scanner stopped:")
            leScanner?.stopScan(scanCallback)
        }
        else { Timber.d("$TAG: stopScanner: no need to stop scanner:") }
    }

    fun stopScanning() {
        Timber.d("$TAG: stopScanning:")
        stopScanner()
    }

    fun switchConnectionValue(newState : Boolean) {
        Timber.d("$TAG: switchConnectionValue: newState=$newState")
        connected = newState
    }
}