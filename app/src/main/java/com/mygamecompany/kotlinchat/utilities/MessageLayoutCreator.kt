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
        this.context = context
    }

    fun createMessage(message: String, sender: Boolean): TextView {
        Timber.d("Creating message... Is sender?: $sender")
        return TextView(context).apply {
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
    }

    private fun createMessageLayoutParams(sender: Boolean, resources: Resources): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
            setMargins(margin, margin, margin, margin)
            gravity = if (sender) Gravity.END else Gravity.START
        }
    }

    fun createConnectionMessage(message: String): TextView {
        Timber.d("Creating connection message...")
        return TextView(context).apply {
                textSize = resources.getDimension(R.dimen.message_text_size)
                setTextColor(Color.WHITE)
                val horizontalPadding: Int = resources.getDimension(R.dimen.message_padding_horizontal).toInt()
                val verticalPadding: Int = resources.getDimension(R.dimen.message_padding_vertical).toInt()
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                layoutParams = createConnectionMessageLayoutParams(resources)
                setBackgroundResource(R.drawable.connection_message)
                text = message
            }
    }

    private fun createConnectionMessageLayoutParams(resources: Resources): LinearLayout.LayoutParams {
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin: Int = resources.getDimension(R.dimen.message_margin).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        layoutParams.gravity = Gravity.CENTER
        return layoutParams
    }
}