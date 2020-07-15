package com.mygamecompany.kotlinchat.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

class Advertiser(private val bluetoothAdapter: BluetoothAdapter) {

    //ADVERTISE CALLBACK
    private val advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Timber.d("Advertising start success.")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Timber.d("Advertising start failure. Error code: $errorCode")
        }
    }

    //VARIABLES
    private var leAdvertiser: BluetoothLeAdvertiser? = null

    //FUNCTIONS
    private fun buildAdvertiseSettings(): AdvertiseSettings {
        Timber.d("Building advertise settings...")
        return AdvertiseSettings.Builder().build()
    }

    private fun buildAdvertiseData(): AdvertiseData {
        Timber.d("Building advertise data...")
        return AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(Constants.SERVICE_UUID))
            .setIncludeDeviceName(false)
            .build()
    }

    private fun buildScanResponseData(): AdvertiseData {
        Timber.d("Building scan response data...")
        return AdvertiseData.Builder()
            .addServiceData(ParcelUuid(Constants.SERVICE_UUID), Repository.username.take(8).toByteArray(Charsets.UTF_8))
            .build()
    }

    private fun createAdvertiser() {
        Timber.d("Creating advertiser...")
        if (bluetoothAdapter.isEnabled) leAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (leAdvertiser == null) Timber.d("Could not create advertiser. LE advertising is not available.")
        else Timber.d("Advertiser created.")
    }

    private fun startAdvertiser() {
        Timber.d("Starting advertising...")
        if (leAdvertiser != null) {
            leAdvertiser?.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), buildScanResponseData(), advertiseCallback )
            Timber.d("Advertising started.")
        }
        else Timber.d("Could not start advertising. Advertiser not created.")
    }

    private fun stopAdvertiser() {
        Timber.d("Stopping advertising...")
        if (leAdvertiser != null) {
            leAdvertiser?.stopAdvertising(advertiseCallback)
            Timber.d("Advertising stopped.")
        } else Timber.d("There is no need to stop advertising.")
    }

    fun startAdvertising() {
        if (leAdvertiser == null) createAdvertiser()
        startAdvertiser()
    }

    fun stopAdvertising() = stopAdvertiser()
}