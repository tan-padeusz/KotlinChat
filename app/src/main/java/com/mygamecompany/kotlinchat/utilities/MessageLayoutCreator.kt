package com.mygamecompany.kotlinchat.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.Repository.TAG
import timber.log.Timber

object MessageLayoutCreator {
    //VARIABLES
    private lateinit var context: Context

    //FUNCTIONS
    fun initializeLayoutCreator(context: Context) {
        Timber.d("$TAG: initializeLayoutCreator:")
        this.context = context
    }

    fun createMessage(message: String, sender: Boolean): TextView {
        Timber.d("$TAG: createMessage: sender=$sender")
        val newView = TextView(context)
        with(newView) {
            text = message
            textSize = resources.getDimension(R.dimen.message_text_size)
            val horizontalPadding: Int = resources.getDimension(R.dimen.message_padding_horizontal).toInt()
            val verticalPadding: Int = resources.getDimension(R.dimen.message_padding_vertical).toInt()
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            layoutParams = when(sender) {
                true -> {
                    setBackgroundResource(R.drawable.my_message)
                    setTextColor(Color.WHITE)
                    createMessageLayoutParams(sender, resources)
                }
                false -> {
                    setBackgroundResource(R.drawable.not_my_message)
                    setTextColor(Color.BLACK)
                    createMessageLayoutParams(sender, resources)
                }
            }
        }
        return newView
    }

    private fun createMessageLayoutParams(sender: Boolean, resources: Resources): LinearLayout.LayoutParams {
        Timber.d("$TAG: createMessageLayoutParams: sender=$sender")
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        layoutParams.gravity = if(sender) Gravity.END
        else Gravity.START
        return layoutParams
    }

    fun createConnectionMessage(username: String, connected: Boolean): TextView {
        Timber.d("$TAG: createConnectionMessage: connected=$connected")
        val newView = TextView(context)
        with(newView) {
            textSize = resources.getDimension(R.dimen.message_text_size)
            val horizontalPadding: Int = resources.getDimension(R.dimen.message_padding_horizontal).toInt()
            val verticalPadding: Int = resources.getDimension(R.dimen.message_padding_vertical).toInt()
            setTextColor(Color.WHITE)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            layoutParams = createConnectionMessageLayoutParams(resources)
            setBackgroundResource(R.drawable.connection_message)
            text = if (connected) username + resources.getString(R.string.fchat_connection_message)
            else username + resources.getString(R.string.fchat_disconnection_message)
        }
        return newView
    }

    private fun createConnectionMessageLayoutParams(resources: Resources): LinearLayout.LayoutParams {
        Timber.d("$TAG: createConnectionMessageLayoutParams:")
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        layoutParams.gravity = Gravity.CENTER
        return layoutParams
    }
}