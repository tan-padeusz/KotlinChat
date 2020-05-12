package com.mygamecompany.kotlinchat.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.data.Repository.TAG
import com.mygamecompany.kotlinchat.databinding.FragmentChatBinding
import com.mygamecompany.kotlinchat.utilities.Constants
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import kotlinx.android.synthetic.main.fragment_chat.*
import timber.log.Timber

class ChatFragment : Fragment()
{
    //VARIABLES
    private lateinit var binding: FragmentChatBinding

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("$TAG: onCreateView:")
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("$TAG: onViewCreated:")
        binding.addressLabelText = "My name: ${Repository.username}"
        binding.addressLabel.visibility = View.VISIBLE

        Repository.receiveMessage().observe(viewLifecycleOwner, Observer {
            Timber.d("$TAG: receiveMessageObserver: received message: ${it[0]}")
            when(it[0]) {
                Constants.TEXT_MESSAGE_SENDER -> binding.messageView.addView(MessageLayoutCreator.createMessage(it.removeRange(0, 1), true))
                Constants.TEXT_MESSAGE_RECEIVER -> binding.messageView.addView(MessageLayoutCreator.createMessage(it.removeRange(0, 1), false))
                Constants.CONNECTION_MESSAGE -> binding.messageView.addView(MessageLayoutCreator.createConnectionMessage(it.removeRange(0,1), true))
                Constants.DISCONNECTION_MESSAGE -> binding.messageView.addView(MessageLayoutCreator.createConnectionMessage(it.removeRange(0,1), false))
                else -> { }
            }
        })

        messageView.setOnClickListener {
            Timber.d("$TAG: messageView: onClick:")
            val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if(inputText.hasFocus()) {
                imm.hideSoftInputFromWindow(inputText.windowToken, 0)
                inputText.clearFocus()
            }
        }

        sendButton.setOnClickListener {
            Timber.d("$TAG: sendButton: onClick:")
            if((inputText.text != null) and (inputText.text.toString() != "")) {
                Repository.sendMessage(inputText.text.toString())
                inputText.text.clear()
            }
        }
    }
}