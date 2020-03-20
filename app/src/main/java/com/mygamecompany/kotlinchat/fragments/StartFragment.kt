package com.mygamecompany.kotlinchat.fragments

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import kotlinx.android.synthetic.main.fragment_start.*
import java.util.*

class StartFragment : Fragment()
{
    //CONSTANTS
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothRequestPermission: Int = 1
    private val locationRequestPermission: Int = 2
    private val timer: Timer = Timer("delay_start")
    private val logTag: String = "KTC_${javaClass.simpleName}"

    //VARIABLES
    private var isBluetoothEnabled: Boolean = false
    private var isLocationEnabled: Boolean = false

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        timer.schedule(object : TimerTask()
        {
            override fun run()
            {
                delayedStartAction()
            }
        }, 2250)

        requestButton.setOnClickListener()
        {
            setPermissionFlags()
            sendPermissionRequests()
        }
    }

    private fun delayedStartAction()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        setPermissionFlags()
        if(checkPermissions())
        {
            navigate()
        }
        else
        {
            sendPermissionRequests()
        }
    }

    private fun checkPermissions(): Boolean
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return isBluetoothEnabled and isLocationEnabled
    }

    private fun sendPermissionRequests()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if (!isBluetoothEnabled)
        {
            Log.d(logTag, "$methodName: requesting bluetooth: ")

            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), bluetoothRequestPermission)
        }

        if (!isLocationEnabled) {
            Log.d(logTag, "$methodName: requesting location: ")

            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), locationRequestPermission)
        }
    }

    private fun setPermissionFlags()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        isBluetoothEnabled = bluetoothAdapter.isEnabled
        isLocationEnabled = ((ContextCompat.checkSelfPermission(activity as Context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        and (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
    }

    private fun navigate()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        createInstances()
        findNavController().navigate(R.id.action_startFragment_to_roomFragment)
    }

    private fun createInstances()
    {
        Client.createInstance(bluetoothAdapter, activity as Context)
        Server.createInstance(bluetoothAdapter, activity as Context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if (requestCode == bluetoothRequestPermission)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                isBluetoothEnabled = true
                if(isLocationEnabled) navigate()
            }
            else
            {
                requestLabel.visibility = View.VISIBLE
                requestButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        if (requestCode == locationRequestPermission)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isLocationEnabled = true
                if (isBluetoothEnabled) navigate()
            }
            else
            {
                requestLabel.visibility = View.VISIBLE
                requestButton.visibility = View.VISIBLE
            }
        }
    }
}