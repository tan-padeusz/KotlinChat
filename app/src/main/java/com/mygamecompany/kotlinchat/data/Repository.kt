package com.mygamecompany.kotlinchat.data

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mygamecompany.kotlinchat.bluetooth.Client
import com.mygamecompany.kotlinchat.bluetooth.Server
import com.mygamecompany.kotlinchat.interfaces.ChatDevice
import com.mygamecompany.kotlinchat.utilities.Constants
import timber.log.Timber

object Repository : ChatDevice {

    var isServer: Boolean = false
    var username: String = ""

    private var client: Client? = null
    private var server: Server? = null

    private val roomList: ArrayList<ChatRoom> = ArrayList()
    private val roomListLiveData: MutableLiveData<List<ChatRoom>> = MutableLiveData()

    fun initializeBluetoothDevices(bluetoothAdapter: BluetoothAdapter, context: Context) {
        client = Client(bluetoothAdapter, context)
        server = Server(bluetoothAdapter, context)
    }

    override fun runBluetoothDevice(run: Boolean) {
        if (isServer) server!!.runBluetoothDevice(run)
        else client!!.runBluetoothDevice(run)
    }

    override fun sendMessage(message: String) {
        if (isServer) server!!.sendMessage(message)
        else client!!.sendMessage(message)
    }

    override fun getLastMessage(): LiveData<String> {
        return if (isServer) server!!.getLastMessage()
        else client!!.getLastMessage()
    }

    fun observeScanResult() {
        if(isServer) {
            Timber.d("Could not observe scan result. Device is server.")
            return
        }
        client!!.getLastScanResult().observeForever {
            val address: String = it.device.address
            val name: String = it.scanRecord?.serviceData?.get(ParcelUuid(Constants.SERVICE_UUID))!!.toString(Charsets.UTF_8)
            roomList.add(ChatRoom(address, name))
            roomListLiveData.postValue(roomList)
        }
    }

    fun getChatRoomList(): LiveData<List<ChatRoom>> = roomListLiveData
}