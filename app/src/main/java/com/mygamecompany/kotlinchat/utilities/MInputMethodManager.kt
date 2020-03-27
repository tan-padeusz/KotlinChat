package com.mygamecompany.kotlinchat.utilities

import android.view.inputmethod.InputMethodManager

object MInputMethodManager
{
    private var imm: InputMethodManager? = null
    fun getInputMethodManager(): InputMethodManager = imm!!
    fun setInputMethodManager(newInputMethodManager: InputMethodManager) { imm = newInputMethodManager }
}