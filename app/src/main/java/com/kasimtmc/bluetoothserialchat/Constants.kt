package com.kasimtmc.bluetoothserialchat

import android.Manifest
import android.os.Build
import java.util.UUID

object Constants {
    const val REQUEST_CODE_PERMISSIONS= 123
    val REQUIRED_PERMISSIONS= arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE)
    val listOfPerms= listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE)
    val SERVICE_UUID: UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
}