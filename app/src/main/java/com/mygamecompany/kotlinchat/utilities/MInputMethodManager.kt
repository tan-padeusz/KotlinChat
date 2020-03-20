package com.mygamecompany.kotlinchat.utilities

import android.view.inputmethod.InputMethodManager

class MInputMethodManager
{
    companion object
    {
        private var imm: InputMethodManager? = null

        fun getInputMethodManager(): InputMethodManager
        {
            return imm!!
        }

        fun setInputMethodManager(newInputMethodManager: InputMethodManager)
        {
            imm = newInputMethodManager
        }
    }
}