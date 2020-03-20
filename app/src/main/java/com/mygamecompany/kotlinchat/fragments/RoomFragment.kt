package com.mygamecompany.kotlinchat.fragments

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.utilities.CurrentRole
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import kotlinx.android.synthetic.main.fragment_room.*

class RoomFragment : Fragment() {

    private val logTag: String = "KTC_${javaClass.simpleName}"
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        return inflater.inflate(R.layout.fragment_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        searchRoom.setOnClickListener()
        {
            Log.d(logTag, "$methodName: searchRoom: onClick: ")

            CurrentRole.setRole(CurrentRole.Role.CLIENT)
            Client.getInstance().enableScan(true)
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }

        startRoom.setOnClickListener()
        {
            Log.d(logTag, "$methodName: startRoom: onClick: ")

            CurrentRole.setRole(CurrentRole.Role.SERVER)
            Server.getInstance().enableAdvertisement(true)
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }
    }
}