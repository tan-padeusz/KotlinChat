package com.mygamecompany.kotlinchat.fragments

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository
import com.mygamecompany.kotlinchat.utilities.MInputMethodManager
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import timber.log.Timber

class MainActivity : AppCompatActivity()
{
    //VARIABLES
    private lateinit var startFragment: StartFragment
    private lateinit var roomFragment: RoomFragment
    private lateinit var chatFragment: ChatFragment

    //FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        setContentView(R.layout.activity_main)
        initialiseLayoutCreator(applicationContext)
        setInputMethodManager()
        initialiseFragments()
        enableScreenTimeout()
    }



    private fun initialiseLayoutCreator(context: Context)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")
        MessageLayoutCreator.initializeLayoutCreator(context)
    }

    private fun setInputMethodManager() {
        Timber.d(Repository.toString())
        MInputMethodManager.setInputMethodManager(getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
    }

    private fun initialiseFragments() {
        Timber.d(Repository.toString())
        startFragment = StartFragment()
        roomFragment = RoomFragment()
        chatFragment = ChatFragment()
    }

    private fun enableScreenTimeout() {
        Timber.d(Repository.toString())
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}