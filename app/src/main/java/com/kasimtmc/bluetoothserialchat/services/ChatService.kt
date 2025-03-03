package com.kasimtmc.bluetoothserialchat.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kasimtmc.bluetoothserialchat.Constants.REQUIRED_PERMISSIONS
import com.kasimtmc.bluetoothserialchat.Constants.SERVICE_UUID
import com.kasimtmc.bluetoothserialchat.GlobalStates.deviceDetails
import com.kasimtmc.bluetoothserialchat.GlobalStates.isChat
import com.kasimtmc.bluetoothserialchat.GlobalStates.remoteDevice
import com.kasimtmc.bluetoothserialchat.GlobalStates.selectedDevice
import com.kasimtmc.bluetoothserialchat.GlobalStates.serverName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class ChatService(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val listener: ServiceListener,
    private val messageListener: MessageListener
) {
    private val uuid: UUID = SERVICE_UUID
    private var serverSocket: BluetoothServerSocket? = null
    private var socketSecure: BluetoothSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var connectedThread: ConnectedThread

    suspend fun startServer() {
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            try {
                withContext(Dispatchers.IO) {
                    serverSocket = adapter.listenUsingRfcommWithServiceRecord(serverName.value, uuid)
                    socketSecure = serverSocket?.accept()
                    listener.onSecureConnection(if (isChat.value && socketSecure?.remoteDevice != null) socketSecure?.remoteDevice else null)
                }
            } catch (e: IOException) {
                Log.e("Tarama", "Tarama hatası", e)
                socketSecure?.close()
                serverSocket?.close()
            }
        }
    }

    suspend fun stopServer() {
        withTimeout(2000) {
            try {
                if (socketSecure?.connectionType == BluetoothSocket.TYPE_RFCOMM) {
                    socketSecure?.close()
                    serverSocket?.close()
                    bluetoothSocket?.close()
                } else {
                    // noooothing
                }
            } catch (e: IOException) {
                Log.e("Server", "Server durdurulamadı", e)
            } catch (e: Exception) { // extra safety fot null pointer exception
                Log.e("Server", "Bilinmeyen bir hata oluştu", e)
            }
        }
    }

    suspend fun connect() {
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            })
        {
            withTimeout(timeMillis= 3000) {
                try {
                    bluetoothSocket= if (isChat.value) {
                        socketSecure
                    } else {
                        selectedDevice!!.createRfcommSocketToServiceRecord(uuid)
                    }
                    // çalışmazsa socketSecure.remoteDevice.createRfcommSocketToServiceRecord(uuid) ile değiştir
                    if (bluetoothSocket?.isConnected == false) {
                        bluetoothSocket?.connect()
                    } else {
                        // nothing to do
                    }
                } catch (e: IOException) {
                    Toast.makeText(context, "Bağlanılamadı", Toast.LENGTH_SHORT).show()
                    Log.e("Bluetooth Service", "Bağlanılamadı", e)
                    bluetoothSocket?.close()
                }
            }
        }
        if (bluetoothSocket?.isConnected == true) {
            inputStream= bluetoothSocket!!.inputStream
            outputStream= bluetoothSocket!!.outputStream
            connectedThread= ConnectedThread(bluetoothSocket!!, inputStream, outputStream)
            connectedThread.start()
        }
        listener.onConnectionStateChanged(bluetoothSocket!!.isConnected)
        deviceDetails.value= if (bluetoothSocket!!.isConnected) "Bağlandı" else "Bağlı değil"
    }

    suspend fun disconnect() {
        withTimeout(timeMillis = 2000) {
            try {
                inputStream.close()
                outputStream.close()
                if (!isChat.value) bluetoothSocket!!.close()
                connectedThread.interrupt()
                remoteDevice.value= null
            } catch (e: IOException) {
                Log.e("Bluetooth Service", "Soket kapatılamadı", e)
            }
        }
        listener.onConnectionStateChanged(bluetoothSocket!!.isConnected)
        deviceDetails.value= if (bluetoothSocket!!.isConnected) "Bağlandı" else "Bağlı değil"
    }

    interface ServiceListener {
        fun onConnectionStateChanged(state: Boolean)
        fun onSecureConnection(device: BluetoothDevice?)
    }

    interface MessageListener {
        fun onMessageReceived(message: String)
        fun onMessageSent(outgoing: String)
    }

    fun sendData(data: String) {
        connectedThread.write(data)
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket, private val inStream: InputStream, private val outStream: OutputStream) : Thread() {

        override fun run() {
            super.run()
            while (socket.isConnected) {
                val buffer= ByteArray(1024)
                var bytes: Int?
                var message: String
                try {
                    bytes= inStream.read(buffer)
                    message= String(buffer, 0, bytes)
                    messageListener.onMessageReceived(message)
                } catch(e: IOException) {
                    break
                }
            }
        }

        fun write(outgoing: String) {
            try {
                if (outgoing.isNotEmpty()) {
                    //socket.outputStream.write(outgoing.toByteArray()) //
                    outStream.write(outgoing.toByteArray())
                    messageListener.onMessageSent(outgoing)
                }
            } catch (e: IOException) {
                Log.e("Bluetooth Service", "Mesaj gönderilemedi", e)
                messageListener.onMessageSent("Mesaj gönderilemedi")
            }
        }
    }

}