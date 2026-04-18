package com.example.chatapp.viewmodels.BluetoothModule

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeScanner
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.entity.BluetoothMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import okio.IOException
import java.io.OutputStream
import java.net.Socket
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BluetoothMessagingViewModel @Inject constructor(
    private val _bluetoothAdapter: BluetoothAdapter,
    private val _bluetoothManager: BluetoothManager
) : ViewModel() {

    private var _bluetoothLeScanner: BluetoothLeScanner? = null

    private val _uuid = UUID.fromString("4a202ae9-4a5b-4ed9-9df5-31d3b58c3b88")
    private val _appName = "Bchat"

    private val _deviceToChat = MutableLiveData<BluetoothDevice?>(null)
    val deviceToChat: LiveData<BluetoothDevice?> = _deviceToChat

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: MutableLiveData<Boolean> = _isConnected

    private val _messageList = MutableLiveData<MutableList<BluetoothMessage>>(mutableListOf())
    val messageList: MutableLiveData<MutableList<BluetoothMessage>> = _messageList

    private var _outputStream: OutputStream? = null

    fun areBluetoothVariablesDefined(): Boolean{
        return !(_bluetoothLeScanner == null || _bluetoothAdapter == null || _bluetoothManager == null)
    }

    @SuppressLint("MissingPermission")
    fun startServer(){
        if (_bluetoothLeScanner == null) {
            _bluetoothLeScanner = _bluetoothAdapter.bluetoothLeScanner
        }
        if(_bluetoothAdapter == null){
            Log.d("server_check", "bluetooth variables undefined, returning")
            return
        }
        Thread{
            try {
                val bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(_appName, _uuid)
                Log.d("server_check", "the socket is open, waiting for a connection...")
                val socket = bluetoothServerSocket.accept()
                if(socket != null){
                    Log.d("server_check", "a device connected")
                    manageConnectedSocket(socket)
                    bluetoothServerSocket.close()
                }
            }
            catch (e: IOException){
                Log.d("server_check", "socket error: ${e.message}")
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun manageConnectedSocket(socket: BluetoothSocket){
        if(_deviceToChat == null)
            return
        if (_deviceToChat.value == null) {
            val remoteDevice = socket.remoteDevice
            _deviceToChat.postValue(remoteDevice)
        }
        val inputStream = socket.inputStream
        _outputStream = socket.outputStream
        Thread{
            val buffer = ByteArray(1024)
            var bytes: Int
            while(true){
                try {
                    updateIsConnected(true)
                    bytes = inputStream.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    val item = BluetoothMessage(1, _deviceToChat?.value?.address, incomingMessage, System.currentTimeMillis().toString(), _deviceToChat?.value?.name)
                    val currentList = _messageList.value?.toMutableList() ?: mutableListOf()
                    currentList.add(item)
                    _messageList.postValue(currentList)
                }
                catch (e: IOException) {
                    Log.d("chat_check", "Connection lost: ${e.message}")
                    updateIsConnected(false)
                    break
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice){
        Thread{
            val socket = device.createRfcommSocketToServiceRecord(_uuid)
            try{
                _bluetoothAdapter.cancelDiscovery()
                socket.connect()
                _deviceToChat.postValue(device)
                Log.d("server_check", "attempting to connnect: ${device.name}")
                Log.d("server_check", "connection is successful: ${device.name}")
                updateIsConnected(true)
                manageConnectedSocket(socket)
            }
            catch (e: IOException){
                Log.d("server_check", "client socket error: ${e.message}")
                try {
                    socket.close()
                    updateIsConnected(false)
                }
                catch (closeException: IOException) {
                    Log.e("server_check", "socket couldn't be closed, ", closeException)
                    updateIsConnected(false)
                }
            }
        }.start()
    }

    fun sendMessage(message: String){
        if(_outputStream == null) {
            Log.d("server_check", "output stream is null, returning")
            return
        }
        Thread{
            try {
                val byte = message.toByteArray(Charsets.UTF_8)
                _outputStream?.write(byte)
                _outputStream?.flush()
                val currentList = _messageList.value?.toMutableList() ?: mutableListOf()
                val item = BluetoothMessage(message = message, timestamp = System.currentTimeMillis().toString(), isSentByMe = true)
                currentList.add(item)
                _messageList.postValue(currentList)
                Log.d("server_check", "flushed")
            }
            catch (e: IOException) {
                Log.e("chat_check", "Something went wrong sending the message: ${e.message}")
            }
        }.start()
    }

    fun updateIsConnected(b: Boolean){
        if(!b)
            Log.e("chat_check", "Something went wrong with the connection")
        _isConnected.postValue(b)
    }

    override fun onCleared() {
        super.onCleared()
    }

}