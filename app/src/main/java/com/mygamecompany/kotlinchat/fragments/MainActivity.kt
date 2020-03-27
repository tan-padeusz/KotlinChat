package com.mygamecompany.kotlinchat.fragments

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.utilities.MInputMethodManager
import com.mygamecompany.kotlinchat.utilities.MessageLayoutCreator
import timber.log.Timber

class MainActivity : AppCompatActivity()
{
    //VARIABLES
    private lateinit var startFragment: StartFragment
    private lateinit var roomFragment: RoomFragment
    private lateinit var chatFragment: ChatFragment
    private var layoutCreator: MessageLayoutCreator? = null

    //FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        setContentView(R.layout.activity_main)
        initialiseLayoutCreator()
        setInputMethodManager()
        initialiseFragments()
        enableScreenTimeout(false)
    }



    private fun initialiseLayoutCreator()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        MessageLayoutCreator.createInstance(this)
        layoutCreator = MessageLayoutCreator.getInstance()
    }

    private fun setInputMethodManager()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        MInputMethodManager.setInputMethodManager(getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
    }

    private fun initialiseFragments()
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: ")

        startFragment = StartFragment()
        roomFragment = RoomFragment()
        chatFragment = ChatFragment()
    }

    private fun enableScreenTimeout(enable: Boolean)
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Timber.d("$methodName: enable: $enable")

        with(window)
        {
            when(enable)
            {
                false -> addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                true -> clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}