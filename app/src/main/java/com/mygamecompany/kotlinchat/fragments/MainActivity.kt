package com.mygamecompany.kotlinchat.fragments

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import timber.log.Timber

class MainActivity : AppCompatActivity()
{
    //VALUES
    private val appTag: String = Repository.TAG

    //VARIABLES
    private lateinit var startFragment: StartFragment
    private lateinit var roomFragment: RoomFragment
    private lateinit var chatFragment: ChatFragment

    //FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d(appTag)
        setContentView(R.layout.activity_main)
        initialiseLayoutCreator(applicationContext)
        initialiseFragments()
        enableScreenTimeout()
    }

    private fun initialiseLayoutCreator(context: Context) {
        Timber.d(appTag)
        MessageLayoutCreator.initializeLayoutCreator(context)
    }

    private fun initialiseFragments() {
        Timber.d(appTag)
        startFragment = StartFragment()
        roomFragment = RoomFragment()
        chatFragment = ChatFragment()
    }

    private fun enableScreenTimeout() {
        Timber.d(appTag)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}