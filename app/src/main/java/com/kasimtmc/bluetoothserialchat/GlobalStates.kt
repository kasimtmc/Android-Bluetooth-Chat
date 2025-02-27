package com.kasimtmc.bluetoothserialchat

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.kasimtmc.bluetoothserialchat.ui.Chat

object GlobalStates {
    private val remote: BluetoothDevice? = null
    //message list, related with Message()
    val messages = mutableStateListOf<Message>()
    // connection state
    val discoverableDuration= mutableIntStateOf( 300)
    val discoveredDevices= mutableStateListOf<BluetoothDevice?>()
    val isConnected = mutableStateOf(false)
    val isPaired = mutableStateOf(false)
    val deviceDetails= mutableStateOf("Eşleştirilmiş")
    var selectedDevice: BluetoothDevice?= null
    val isChat= mutableStateOf(false)
    val hasRemote= mutableStateOf(false)
    val remoteDevice= mutableStateOf(remote)
    val serverName= mutableStateOf("Chat Server")

    @Composable
    fun dynamicColors(context: Context): ColorScheme {
        val schemeBase: ColorScheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        val scheme by remember { mutableStateOf(schemeBase) }
        return scheme
    }

}