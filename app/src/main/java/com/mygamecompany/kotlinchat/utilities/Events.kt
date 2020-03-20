package com.mygamecompany.kotlinchat.utilities

class Events
{
    class SetMessage(val message : String)
    class ConnectionMessage(val deviceAddress : String, val isConnected : Boolean)
}