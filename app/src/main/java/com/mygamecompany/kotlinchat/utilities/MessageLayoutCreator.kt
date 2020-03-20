package com.mygamecompany.kotlinchat.utilities

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.mygamecompany.kotlinchat.R

class MessageLayoutCreator(private val context: Context)
{
    private val logTag: String = "KTC_${javaClass.simpleName}"

    fun createMessage(message: String, sender: Boolean): TextView
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

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

    fun createConnectionMessage(address: String, connected: Boolean): TextView
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val newView = TextView(context)
        with(newView)
        {
            textSize = 17.toFloat()
            setTextColor(Color.WHITE)
            setPadding(20, 10 ,20 ,10)
            layoutParams = createConnectionMessageLayoutParams()
            setBackgroundResource(R.drawable.connection_message)
            text = when(connected)
            {
                true -> "Connected to: $address"
                false -> "Disconnected with: $address"
            }
        }
        return newView
    }

    private fun createMessageLayoutParams(sender: Boolean): LinearLayout.LayoutParams
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val layoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(15, 15, 15, 15)
        layoutParams.gravity = when(sender)
        {
            true -> Gravity.END
            false -> Gravity.START
        }
        return layoutParams
    }

    private fun createConnectionMessageLayoutParams(): LinearLayout.LayoutParams
    {
        val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
        Log.d(logTag, "$methodName: ")

        val layoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(15, 15, 15, 15)
        layoutParams.gravity = Gravity.CENTER
        return layoutParams
    }

    companion object
    {
        private val logTag: String = "KTC_${MessageLayoutCreator::class.java.simpleName}"
        private var instance: MessageLayoutCreator? = null

        fun createInstance(context: Context)
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            instance = MessageLayoutCreator(context)
        }

        fun getInstance(): MessageLayoutCreator
        {
            val methodName: String = object {}.javaClass.enclosingMethod?.name ?: "unknown name"
            Log.d(logTag, "$methodName: ")

            return instance!!
        }
    }
}