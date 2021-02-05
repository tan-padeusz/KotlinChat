package com.mygamecompany.kotlinchat.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.application.DaggerAppComponent
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.databinding.FragmentMenuBinding
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import com.mygamecompany.kotlinchat.utilities.PermissionHandler
import com.mygamecompany.kotlinchat.viewmodels.MenuViewModel
import kotlinx.android.synthetic.main.fragment_menu.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    @Inject lateinit var menuViewModel: MenuViewModel
    private lateinit var usernameFilePath: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        usernameFilePath = requireContext().filesDir.absolutePath + File.separator + "username.ktch"

        val components = DaggerAppComponent.factory().create(requireContext())
        components.inject(this)

        MessageLayoutCreator.initializeLayoutCreator(requireContext())
        setListeners()
        binding.usernameInput.setText(loadUsername())
        PermissionHandler.setFlags()
        observePermissionHandlerStatus()
        return binding.root
    }

    private fun setListeners() {
        with(binding) {
            with(usernameInput.text.toString()) {
                searchRoom.isEnabled = this.isNotEmpty()
                startRoom.isEnabled = this.isNotEmpty()
            }

            usernameInput.addTextChangedListener(object: TextWatcher {
                private fun pass(message: String) = Timber.d(message)

                override fun afterTextChanged(s: Editable?) {
                    with(s.toString()) {
                        pass("afterTextChanged: $this")
                        searchRoom.isEnabled = this.isNotEmpty() and PermissionHandler.status.value!!
                        startRoom.isEnabled = this.isNotEmpty() and PermissionHandler.status.value!!
                        Repository.username = this
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = pass("beforeTextChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = pass("onTextChanged")
            })

            searchRoom.setOnClickListener {
                Timber.d("searchRoom: onClick:")
                with(Repository) {
                    menuViewModel.stopBLEDevice()
                    isServer = false
                    menuViewModel.runBLEDevice()
                    saveUsername(usernameInput.text.toString())
                }
                findNavController().navigate(R.id.action_menuFragment_to_roomsFragment)
            }

            startRoom.setOnClickListener {
                Timber.d("startRoom: onClick:")
                with(Repository) {
                    menuViewModel.stopBLEDevice()
                    isServer = true
                    menuViewModel.runBLEDevice()
                    saveUsername(usernameInput.text.toString())
                }
                findNavController().navigate(R.id.action_menuFragment_to_chatFragment)
            }
        }
    }

    private fun observePermissionHandlerStatus() {
        PermissionHandler.status.observe(viewLifecycleOwner, Observer {
            binding.searchRoom.isEnabled = it and usernameInput.text.toString().isNotEmpty()
            binding.startRoom.isEnabled = it and usernameInput.text.toString().isNotEmpty()
        })
    }

    private fun loadUsername(): String {
        Timber.d(usernameFilePath)
        val file: File = File(usernameFilePath)
        return if(file.exists()) file.readLines()[0]
        else {
            Timber.d("Username file does not exists yet!")
            ""
        }
    }

    private fun saveUsername(username: String) {
        val file = File(usernameFilePath)
        if (!file.exists()) {
            Timber.d("Username file does not exists yet! Creating username file...")
            file.createNewFile()
        }
        file.writeText(username)
    }
}