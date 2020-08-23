package com.mygamecompany.kotlinchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.adapters.ChatRoomAdapter
import com.mygamecompany.kotlinchat.adapters.ChatRoomClickListener
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.databinding.FragmentRoomsBinding
import com.mygamecompany.kotlinchat.utilities.PermissionHandler

class RoomsFragment : Fragment() {
    private lateinit var binding: FragmentRoomsBinding
    private lateinit var roomsAdapter: ChatRoomAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = FragmentRoomsBinding.inflate(inflater, container, false)
        initializeComponentsAndObservers()
        return binding.root
    }

    private fun initializeComponentsAndObservers() {
        val llm = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        roomsAdapter = ChatRoomAdapter(ChatRoomClickListener { position ->
            roomOnClick(position)
        })

        with(binding.roomsList) {
            layoutManager = llm
            adapter = roomsAdapter
        }

        Repository.getFoundChatRooms()?.observe(viewLifecycleOwner, Observer {
            roomsAdapter.submitList(it)
            roomsAdapter.notifyDataSetChanged()
        })

        Repository.isConnectedToServer()?.observe(viewLifecycleOwner, Observer {
            if (it) findNavController().navigate(R.id.action_roomsFragment_to_chatFragment)
        })

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_roomsFragment_to_menuFragment)
        }

        observePermissionHandlerStatus()
    }

    private fun observePermissionHandlerStatus() {
        PermissionHandler.status.observe(viewLifecycleOwner, Observer {
            val controller = findNavController()
            if (!it and (controller.currentDestination?.id != R.id.menuFragment)) PermissionHandler.showPermissionAlert(controller)
        })
    }

    private fun roomOnClick(position: Int) {
        Repository.connectToServer(position)
    }
}