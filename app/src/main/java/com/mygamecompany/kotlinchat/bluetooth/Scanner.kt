package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Scanner(private val bluetoothAdapter : BluetoothAdapter, private val context : Context, private val clientCallback : BluetoothGattCallback) {
    //SCAN CALLBACK
    private val scanCallback : ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if(result?.device?.address != null)  {
                Timber.d("Found new chat room. Address: ${result.device!!.address}")
                lastScanResult.postValue(result)
            }
        }
    }

    //VALUES
    private val lastScanResult: MutableLiveData<ScanResult> = MutableLiveData()

    //VARIABLES
    private var connected : Boolean = false
    private var leScanner : BluetoothLeScanner? = null

    //FUNCTIONS
    private fun buildScanFilters(): List<ScanFilter> {
        Timber.d("Building scan filters...")
        return arrayListOf(
            ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Constants.SERVICE_UUID))
            .build()
        )
    }

    private fun buildScanSettings() : ScanSettings {
        Timber.d("Building scan settings...")
        return ScanSettings.Builder().build()
    }

    private fun checkUUIDEquality(uuidOne : String, uuidTwo: String): Boolean {
        Timber.d("Checking UUID equality...")
        if(uuidOne.length != uuidTwo.length) {
            Timber.d("Wrong input data given. Result: false.")
            return false
        }
        for(n in 0..8) if(uuidOne[n] != uuidTwo[n]) return false
        return true
    }

    private fun connect(device : BluetoothDevice) {
        Timber.d("Connecting to device with address: ${device.address}")
        device.connectGatt(context, false, clientCallback)
        stopScanning()
    }

    private fun createScanner() {
        Timber.d(" createScanner:")
        if(leScanner != null) { Timber.d(" createScanner: scanner already created:"); return; }
        if(bluetoothAdapter.isEnabled) {
            Timber.d(" createScanner: scanner created:")
            leScanner = bluetoothAdapter.bluetoothLeScanner
        }
        else {
            Timber.d(" createScanner: bluetooth is not enabled: ")
            return
        }
        if(leScanner == null) { Timber.d(" createScanner: LE scan is not available:") }
    }


    private fun startScanner() {
        Timber.d(" startScanner:")
        if(leScanner != null) {
            Timber.d(" startScanner: scanning started:")
            leScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
        }
        else { Timber.d(" startScanner: LE scan not available:") }
    }

    fun startScanning() {
        Timber.d(" startScanning:")
        createScanner()
        startScanner()
    }

    private fun stopScanner() {
        Timber.d(" stopScanner:")
        if(leScanner != null) {
            Timber.d(" stopScanner: scanner stopped:")
            leScanner?.stopScan(scanCallback)
        }
        else { Timber.d(" stopScanner: no need to stop scanner:") }
    }

    fun stopScanning() {
        Timber.d(" stopScanning:")
        stopScanner()
    }

    fun switchConnectionValue(newState : Boolean) {
        Timber.d(" switchConnectionValue: newState=$newState")
        connected = newState
    }

    fun getLastScanResult(): LiveData<ScanResult> = lastScanResult
}