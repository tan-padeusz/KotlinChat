package com.mygamecompany.kotlinchat.utilities

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository
import timber.log.Timber

object MessageLayoutCreator {

    private var context: Context? = null

    //FUNCTIONS
    fun initializeLayoutCreator(context: Context) {
        this.context = context
    }

    fun createMessage(message: String, sender: Boolean): TextView {
        Timber.d(Repository.toString())
        val newView = TextView(context)
        with(newView)
        {
            text = message
            textSize = 17.toFloat()
            setPadding(20, 10, 20, 10)
            layoutParams = when(sender)
            {
                true ->
                {
                    setBackgroundResource(R.drawable.my_message)
                    setTextColor(Color.WHITE)
                    createMessageLayoutParams(sender)
                }
                false ->
                {
                    setBackgroundResource(R.drawable.outer_message)
                    setTextColor(Color.BLACK)
                    createMessageLayoutParams(sender)
                }
            }
        }
        return newView
    }

    fun createConnectionMessage(username: String, connected: Boolean): TextView {
        Timber.d(Repository.toString())
        val newView = TextView(context)
        with(newView) {
            textSize = 17.toFloat()
            setTextColor(Color.WHITE)
            setPadding(20, 10 ,20 ,10)
            layoutParams = createConnectionMessageLayoutParams()
            setBackgroundResource(R.drawable.connection_message)
            text = if (connected) "$username has connected"
            else "$username has disconnected"
        }
        return newView
    }

    private fun createMessageLayoutParams(sender: Boolean): LinearLayout.LayoutParams {
        Timber.d(Repository.toString())
        val layoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(15, 15, 15, 15)
        layoutParams.gravity = if(sender) Gravity.END
        else Gravity.START
        return layoutParams
    }

    private fun createConnectionMessageLayoutParams(): LinearLayout.LayoutParams {
        Timber.d(Repository.toString())
        val layoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(15, 15, 15, 15)
        layoutParams.gravity = Gravity.CENTER
        return layoutParams
    }
}