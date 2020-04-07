package com.mygamecompany.kotlinchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository
import kotlinx.android.synthetic.main.fragment_room.*
import timber.log.Timber

class RoomFragment : Fragment() {

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        return inflater.inflate(R.layout.fragment_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("")

        searchRoom.setOnClickListener {
            Timber.d("searchRoom: onClick: ")
            with(Repository) {
                username = usernameInput.text.toString()
                runDevice(false)
                isServer = false
                runDevice(true)
            }
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }

        startRoom.setOnClickListener {
            Timber.d("startRoom: onClick: ")
            with(Repository) {
                username = usernameInput.text.toString()
                runDevice(false)
                isServer = true
                runDevice(true)
            }
            findNavController().navigate(R.id.action_roomFragment_to_chatFragment)
        }
    }
}