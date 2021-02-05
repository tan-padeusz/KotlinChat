package com.mygamecompany.kotlinchat.application

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.mygamecompany.kotlinchat.bluetooth.BLEClient
import com.mygamecompany.kotlinchat.bluetooth.BLEServer
import com.mygamecompany.kotlinchat.fragments.ChatFragment
import com.mygamecompany.kotlinchat.fragments.MenuFragment
import com.mygamecompany.kotlinchat.fragments.RoomsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()): AppComponent
    }

    fun getClient(): BLEClient
    fun getServer(): BLEServer

    fun inject(fragment: MenuFragment)
    fun inject(fragment: RoomsFragment)
    fun inject(fragment: ChatFragment)
}