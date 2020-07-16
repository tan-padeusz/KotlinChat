package com.mygamecompany.kotlinchat.fragments

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    //VARIABLES
    private lateinit var startFragment: StartFragment
    private lateinit var roomFragment: RoomFragment
    private lateinit var chatFragment: ChatFragment

    //FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate:")
        setContentView(R.layout.activity_main)
        initializeLayoutCreator(applicationContext)
        initializeFragments()
        disableScreenTimeout()
    }

    private fun initializeLayoutCreator(context: Context) {
        Timber.d("initializeLayoutCreator")
        MessageLayoutCreator.initializeLayoutCreator(context)
    }

    private fun initializeFragments() {
        Timber.d("initializeFragments:")
        startFragment = StartFragment()
        roomFragment = RoomFragment()
        chatFragment = ChatFragment()
    }

    private fun disableScreenTimeout() {
        Timber.d("disableScreenTimeout:")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}