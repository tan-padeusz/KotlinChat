package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.utilities.*
import timber.log.Timber

class Scanner(private val bluetoothAdapter : BluetoothAdapter) {
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
    private var leScanner : BluetoothLeScanner? = null

    //FUNCTIONS
    fun getLastScanResult(): LiveData<ScanResult> = lastScanResult

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

    private fun createScanner() {
        Timber.d("Creating scanner...")
        if (leScanner != null) {
            Timber.d("Could not create scanner. It is already created.")
            return
        }
        if (bluetoothAdapter.isEnabled) {
            Timber.d(" createScanner: scanner created:")
            leScanner = bluetoothAdapter.bluetoothLeScanner
            Timber.d(if (leScanner != null) "Scanner created." else "Could not create scanner. LE scanning is not available.")
        }
        else Timber.d("Could not create scanner. Bluetooth is not enabled.")
    }

    private fun startScan() {
        Timber.d(" startScanner:")
        if (leScanner != null) {
            leScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
            Timber.d(" startScanner: scanning started:")
        }
        else { Timber.d(" startScanner: LE scan not available:") }
    }

    private fun stopScan() {
        Timber.d(" stopScanner:")
        if (leScanner != null) {
            Timber.d(" stopScanner: scanner stopped:")
            leScanner?.stopScan(scanCallback)
        }
        else { Timber.d(" stopScanner: no need to stop scanner:") }
    }

    fun startScanning() {
        Timber.d(" startScanning:")
        createScanner()
        startScan()
    }

    fun stopScanning() {
        Timber.d(" stopScanning:")
        stopScan()
    }
}