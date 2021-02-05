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
import javax.inject.Inject

class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    @Inject lateinit var menuViewModel: MenuViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)

        val components = DaggerAppComponent.factory().create(requireContext())
        components.inject(this)

        MessageLayoutCreator.initializeLayoutCreator(requireContext())
        setListeners()
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
                override fun afterTextChanged(s: Editable?) {
                    pass("afterTextChanged")
                    with(s.toString()) {
                        searchRoom.isEnabled = this.isNotEmpty() and PermissionHandler.status.value!!
                        startRoom.isEnabled = this.isNotEmpty() and PermissionHandler.status.value!!
                        Repository.username = this
                    }
                }

                private fun pass(message: String) = Timber.d(message)
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = pass("beforeTextChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = pass("onTextChanged")
            })

            searchRoom.setOnClickListener {
                Timber.d("searchRoom: onClick:")
                with(Repository) {
                    menuViewModel.stopBLEDevice()
                    isServer = false
                    menuViewModel.runBLEDevice()
                }
                findNavController().navigate(R.id.action_menuFragment_to_roomsFragment)
            }

            startRoom.setOnClickListener {
                Timber.d("startRoom: onClick:")
                with(Repository) {
                    menuViewModel.stopBLEDevice()
                    isServer = true
                    menuViewModel.runBLEDevice()
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
}