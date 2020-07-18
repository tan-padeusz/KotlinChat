package com.mygamecompany.kotlinchat.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.mygamecompany.kotlinchat.R
import timber.log.Timber

object MessageLayoutCreator {
    //VARIABLES
    private var context: Context? = null

    //FUNCTIONS
    fun initializeLayoutCreator(context: Context) {
        Timber.d("initializeLayoutCreator")
        this.context = context
    }

    fun createMessage(message: String, sender: Boolean): TextView {
        Timber.d("createMessage: sender=$sender")
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
            return this
        }
    }

    private fun createMessageLayoutParams(sender: Boolean, resources: Resources): LinearLayout.LayoutParams {
        Timber.d("createMessageLayoutParams: sender=$sender")
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        layoutParams.gravity = if(sender) Gravity.END
        else Gravity.START
        return layoutParams
    }

    fun createConnectionMessage(message: String): TextView {
        val newView = TextView(context)
        with(newView) {
            textSize = resources.getDimension(R.dimen.message_text_size)
            val horizontalPadding: Int = resources.getDimension(R.dimen.message_padding_horizontal).toInt()
            val verticalPadding: Int = resources.getDimension(R.dimen.message_padding_vertical).toInt()
            setTextColor(Color.WHITE)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            layoutParams = createConnectionMessageLayoutParams(resources)
            setBackgroundResource(R.drawable.connection_message)
            text = message
            return this
        }
    }

    private fun createConnectionMessageLayoutParams(resources: Resources): LinearLayout.LayoutParams {
        Timber.d("createConnectionMessageLayoutParams:")
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        layoutParams.gravity = Gravity.CENTER
        return layoutParams
    }
}