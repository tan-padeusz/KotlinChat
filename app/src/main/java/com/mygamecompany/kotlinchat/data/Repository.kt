package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.Uri
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

object Repository : ChatDevice {

    //VALUES
    private val roomListLiveData: MutableLiveData<List<ChatRoom>> = MutableLiveData()

    //VARIABLES
    var isServer: Boolean = false
    var username: String = ""
    private var client: Client? = null
    private var server: Server? = null

    //FUNCTIONS
    override fun getLastMessage(): LiveData<String> = if (isServer) server!!.getLastMessage() else client!!.getLastMessage()
    override fun getLastConnectionMessage(): LiveData<String> = if (isServer) server!!.getLastConnectionMessage() else client!!.getLastConnectionMessage()
    override fun getLastImageUri(): LiveData<Uri> = if (isServer) server!!.getLastImageUri() else client!!.getLastImageUri()
    override fun sendMessage(message: String) = if (isServer) server!!.sendMessage(message) else client!!.sendMessage(message)
    override fun sendConnectionMessage(connected: Boolean) = if (isServer) server!!.sendConnectionMessage(connected) else client!!.sendConnectionMessage(connected)
    fun getChatRoomList(): LiveData<List<ChatRoom>> = roomListLiveData

    fun initializeBluetoothDevices(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    override fun runBluetoothDevice(run: Boolean) {
        if (isServer) server!!.runBluetoothDevice(run)
        else client!!.runBluetoothDevice(run)
    }

    fun observeScanResult() {
        if(isServer) {
            Timber.d("Could not observe scan result. Device is server.")
            return
        }
        client!!.getFoundChatRooms().observeForever {
            val roomList: ArrayList<ChatRoom> = ArrayList()
            for (result in it) {
                val address: String = result.device.address
                val name: String = result.scanRecord?.serviceData?.get(ParcelUuid(Constants.SERVICE_UUID))!!.toString(Charsets.UTF_8)
                roomList.add(ChatRoom(address, name))
                roomListLiveData.postValue(roomList)
            }
        }
    }
}