package com.mygamecompany.kotlinchat.fragments

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.application.DaggerAppComponent
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.databinding.FragmentChatBinding
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import com.mygamecompany.kotlinchat.utilities.PermissionHandler
import com.mygamecompany.kotlinchat.viewmodels.ChatViewModel
import kotlinx.android.synthetic.main.fragment_chat.view.*
import timber.log.Timber
import javax.inject.Inject

class ChatFragment : Fragment() {
    //VARIABLES
    private lateinit var binding: FragmentChatBinding
    @Inject lateinit var chatViewModel: ChatViewModel

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("onCreateView:")
        binding = FragmentChatBinding.inflate(inflater, container, false).also {
            it.addressLabel.text = String.format(getString(R.string.fchat_my_name_label_text), Repository.username)
            it.addressLabel.visibility = View.VISIBLE
        }
        val components = DaggerAppComponent.factory().create(requireContext())
        components.inject(this)

        setupObservers()
        return binding.root
    }

    private fun setupObservers() {
        chatViewModel.getLastMessage().observe(viewLifecycleOwner, Observer {
            binding.messageView.addView(MessageLayoutCreator.createMessage(it, false))
        })

        chatViewModel.getLastConnectionMessage().observe(viewLifecycleOwner, Observer {
            binding.messageView.addView(MessageLayoutCreator.createConnectionMessage(it))
        })

        binding.messageView.setOnClickListener {
            hideKeyboard()
        }

        binding.scrollView.setOnClickListener {
            hideKeyboard()
        }

        binding.sendButton.setOnClickListener {
            val message = binding.inputText.text.toString()
            chatViewModel.sendMessage(message)
            binding.scrollView.messageView.addView(MessageLayoutCreator.createMessage(message, true))
            binding.inputText.text.clear()
        }

        binding.inputText.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) hideKeyboard()
            return@setOnKeyListener true
        }

        chatViewModel.isConnectedToServer()?.observe(viewLifecycleOwner, Observer {
            if (!it) buildDisconnectAlertDialog()
        })

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            buildQuitAlertDialog()
        }

        observePermissionHandlerStatus()
    }

    private fun observePermissionHandlerStatus() {
        PermissionHandler.status.observe(viewLifecycleOwner, Observer {
            val controller = findNavController()
            if (!it and (controller.currentDestination?.id != R.id.menuFragment)) PermissionHandler.showPermissionAlert(controller)
        })
    }

    private fun buildQuitAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.fchat_quit_dialog_message))
            .setPositiveButton(getString(R.string.fchat_dialog_positive_button))  { _, _ ->
                if (Repository.isServer) chatViewModel.forceDisconnection()
                else chatViewModel.disconnectFromServer()
                findNavController().navigate(R.id.action_chatFragment_to_menuFragment)
            }
            .setNegativeButton(getString(R.string.fchat_dialog_negative_button)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun buildDisconnectAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.fchat_disconnect_dialog_message))
            .setNeutralButton(getString(R.string.fchat_dialog_ok_button)) { _, _ ->
                findNavController().navigate(R.id.action_chatFragment_to_menuFragment)
            }
            .create()
            .show()
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputText.windowToken, 0)
        binding.inputText.clearFocus()
    }
}