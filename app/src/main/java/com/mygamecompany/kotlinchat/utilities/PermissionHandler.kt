package com.mygamecompany.kotlinchat.utilities

import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import androidx.navigation.NavController
import com.ckdroid.dynamicpermissions.PermissionStatus
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import java.lang.ClassCastException
import com.ckdroid.dynamicpermissions.PermissionUtils
import com.mygamecompany.kotlinchat.R

object PermissionHandler: BroadcastReceiver() {
    //VALUES
    val status: MutableLiveData<Boolean> = MutableLiveData()

    //VARIABLES
    private lateinit var context: Context
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var locationManager: LocationManager

    private var isBluetoothEnabled: Boolean = false
    private var isLocationEnabled: Boolean = false
    private var isLocationPermissionGranted: Boolean = false

    //REQUEST CODES
    object RequestCodes {
        const val REQUEST_BLUETOOTH_CODE: Int = 0
        const val REQUEST_LOCATION_CODE: Int = 1
        const val REQUEST_LOCATION_PERMISSION_CODE: Int = 2
    }

    //FUNCTIONS
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            Timber.d("Bluetooth state changed. New state: ${if (bluetoothAdapter.isEnabled) "enabled" else "disabled"}.")
            setFlags()
            return
        }

        if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            Timber.d("Location state changed. New state: ${if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) "enabled" else "disabled"}.")
            setFlags()
            return
        }

        Timber.d("Unknown action received.")
    }

    fun initializePermissionHandler(context: Context) {
        this.context = context
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        locationManager = this.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setFlags()
    }

    fun onActivityResult(requestCode: Int) {
        if (requestCode == RequestCodes.REQUEST_BLUETOOTH_CODE) {
            Timber.d("onActivityResult: REQUEST_BLUETOOTH_CODE.")
            setFlags()
            return
        }

        if (requestCode == RequestCodes.REQUEST_LOCATION_CODE) {
            Timber.d("onActivityResult: REQUEST_LOCATION_CODE.")
            setFlags()
            return
        }

        Timber.d("onActivityResult: REQUEST_UNKNOWN_CODE.")
    }

    fun onRequestPermissionResults(requestCode: Int) {
        if (requestCode == RequestCodes.REQUEST_LOCATION_PERMISSION_CODE) {
            Timber.d("onRequestPermissionResults: REQUEST_LOCATION_PERMISSION_CODE.")
            val result = PermissionUtils.checkAndRequestPermissions(context as Activity, mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), RequestCodes.REQUEST_LOCATION_PERMISSION_CODE, false)
            if ((result.finalStatus == PermissionStatus.ALLOWED) or (result.finalStatus == PermissionStatus.NOT_GIVEN)) setFlags()
        }

        Timber.d("onRequestPermissionResults: REQUEST_UNKNOWN_PERMISSION_CODE.")
    }

    fun setFlags() {
        isBluetoothEnabled = bluetoothAdapter.isEnabled
        isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isLocationPermissionGranted = checkLocationPermissionStatus()
        val permissionsGranted = checkFlags()
        if (permissionsGranted != status.value) status.postValue(permissionsGranted)
    }

    private fun checkLocationPermissionStatus(): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            .and((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
    }

    private fun checkFlags(): Boolean {
        return isBluetoothEnabled and isLocationEnabled and isLocationPermissionGranted
    }

    fun requestPermissions() {
        if (!isBluetoothEnabled) requestEnableBluetooth()
        if (!isLocationEnabled) requestEnableLocation()
        if (!isLocationPermissionGranted) requestGrantLocationPermission()
    }

    private fun requestEnableBluetooth() {
        Timber.d("Requesting enable bluetooth...")
        (context as Activity).startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), RequestCodes.REQUEST_BLUETOOTH_CODE)
    }

    private fun requestEnableLocation() {
        Timber.d("Requesting enable location...")
        val locationRequest = LocationRequest().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY }
        val settingsBuilder = LocationSettingsRequest.Builder().apply { addLocationRequest(locationRequest) }
        val requestResult = LocationServices.getSettingsClient(context).checkLocationSettings(settingsBuilder.build())

        requestResult.addOnCompleteListener { task ->
            try { task.getResult(ApiException::class.java) }
            catch (ex: ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvable = ex as ResolvableApiException
                            resolvable.startResolutionForResult(context as Activity, RequestCodes.REQUEST_LOCATION_CODE)
                        }
                        catch (ex: IntentSender.SendIntentException) { Timber.d("Caught SendIntentException.") }
                        catch (ex: ClassCastException) { Timber.d("Caught ClassCastException.") }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { Timber.d("Settings change unavailable.") }
                }
            }
        }
    }

    private fun requestGrantLocationPermission() {
        Timber.d("Requesting grant location permission...")
        (context as Activity).requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), RequestCodes.REQUEST_LOCATION_PERMISSION_CODE)
    }

    fun showPermissionAlert(navController: NavController) {
        val dialog: AlertDialog? = (context as Activity).let {
            val builder = AlertDialog.Builder(it).apply {
                setTitle("Permission Alert!")
                setMessage("At leas one of required permissions was revoked! Returning to Main Menu...")
                setNeutralButton("UNDERSTOOD") { _, _ ->
                    if (navController.currentDestination?.id != R.id.menuFragment) navController.navigate(R.id.menuFragment)
                }
            }
            builder.create()
        }
        dialog?.show()
    }
}