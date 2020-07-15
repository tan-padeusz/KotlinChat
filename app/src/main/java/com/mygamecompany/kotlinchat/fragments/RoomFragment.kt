package com.mygamecompany.kotlinchat.fragments

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.data.Repository.TAG
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import kotlinx.android.synthetic.main.fragment_room.*
import timber.log.Timber

class RoomFragment : Fragment() {

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Timber.d("$TAG: onCreateView:")
        return inflater.inflate(R.layout.fragment_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("$TAG: onViewCreated:")
        Repository.initializeBluetoothDevices(BluetoothAdapter.getDefaultAdapter(), requireContext())
        MessageLayoutCreator.initializeLayoutCreator(requireContext())

        searchRoom.setOnClickListener {
            Timber.d("$TAG: searchRoom: onClick: ")
            with(Repository) {
                username = usernameInput.text.toString()
                runBluetoothDevice(false)
                isServer = false
                runBluetoothDevice(true)
            }
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }

        startRoom.setOnClickListener {
            Timber.d("$TAG: startRoom: onClick: ")
            with(Repository) {
                username = usernameInput.text.toString()
                runBluetoothDevice(false)
                isServer = true
                runBluetoothDevice(true)
            }
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }
    }
}