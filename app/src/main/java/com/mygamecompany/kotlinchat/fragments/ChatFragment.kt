package com.mygamecompany.kotlinchat.fragments

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.utilities.MInputMethodManager
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import kotlinx.android.synthetic.main.fragment_chat.*
import com.mygamecompany.kotlinchat.databinding.FragmentChatBinding
import com.mygamecompany.kotlinchat.utilities.Constants
import com.mygamecompany.kotlinchat.utilities.Events
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class ChatFragment : Fragment()
{
    //CONSTANTS
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //VARIABLES
    private lateinit var binding: FragmentChatBinding

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

//        registerEventBus()
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("")
        binding.addressLabelText = "My name: ${Repository.username}"
        binding.addressLabel.visibility = View.VISIBLE

        Repository.receiveMessage().observe(viewLifecycleOwner, Observer {
            when(it[0]) {
                Constants.TEXT_MESSAGE_SENDER -> binding.messageView.addView(MessageLayoutCreator.createMessage(it.removeRange(0, 1), true))
                Constants.TEXT_MESSAGE_RECEIVER -> binding.messageView.addView(MessageLayoutCreator.createMessage(it.removeRange(0, 1), false))
                Constants.CONNECTION_MESSAGE -> binding.messageView.addView(MessageLayoutCreator.createConnectionMessage(it.removeRange(0,1), true))
                Constants.DISCONNECTION_MESSAGE -> binding.messageView.addView(MessageLayoutCreator.createConnectionMessage(it.removeRange(0,1), false))
                else -> { }
            }
        })

        messageView.setOnClickListener() {
            Timber.d("messageView: onClick: ")

            val imm : InputMethodManager = MInputMethodManager.getInputMethodManager()
            if(inputText.hasFocus()) {
                imm.hideSoftInputFromWindow(inputText.windowToken, 0)
                inputText.clearFocus()
            }
        }

        sendButton.setOnClickListener() {
            Timber.d("sendButton: onClick: ")

            if((inputText.text != null) and (inputText.text.toString() != ""))
            {
                Repository.sendMessage(inputText.text.toString())
                inputText.text.clear()


                /*
                when(CurrentRole.getRole())
                {
                    CurrentRole.Role.CLIENT ->
                    {
                        val message: String = inputText.text.toString()
                        if(Client.getInstance().isConnected())
                        {
                            Timber.d("$methodName: sendButton: onClick: as CLIENT sending: $message: ")

                            messageView.addView(layoutCreator.createMessage(message, true))
                            Client.getInstance().sendMessageToServer(bluetoothAdapter.name, message)
                            inputText.text.clear()
                        }
                    }
                    CurrentRole.Role.SERVER ->
                    {
                        val message: String = inputText.text.toString()
                        if(Server.getInstance().hasConnectedDevices())
                        {
                            Timber.d("$methodName: sendButton: onClick: as SERVER sending: $message: ")

                            messageView.addView(layoutCreator.createMessage(message, true))
                            Server.getInstance().sendMessageToClient(bluetoothAdapter.name, message)
                            inputText.text.clear()
                        }
                    }
                    CurrentRole.Role.NONE ->
                    {
                        Timber.d("$methodName: sendButton: onClick: role: NONE: ")
                    }
                }*/
            }
        }
    }

//    private fun registerEventBus()
//    {
//        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
//        Timber.d("$methodName: ")
//
//        if(!EventBus.getDefault().isRegistered(this))
//        {
//            Timber.d("$methodName: EventBus registered: ")
//            EventBus.getDefault().register(this)
//        }
//        else { Timber.d("$methodName: EventBus already registered: ") }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun connectionMessage(event : Events.ConnectionMessage)
//    {
//        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
//        Timber.d("$methodName: ")
//
//        messageView.addView(layoutCreator.createConnectionMessage(event.deviceAddress, event.isConnected))
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun setMessage(event : Events.SetMessage)
//    {
//        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
//        Timber.d("$methodName: ")
//
//        messageView.addView(layoutCreator.createMessage(event.message, false))
//    }
}