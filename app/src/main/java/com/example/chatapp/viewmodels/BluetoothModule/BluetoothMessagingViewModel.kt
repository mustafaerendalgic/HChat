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
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.entity.BluetoothDeviceListItem
import com.example.chatapp.data.entity.BluetoothMessage
import com.example.chatapp.data.entity.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BluetoothMessagingViewModel @Inject constructor(
    private val _bluetoothAdapter: BluetoothAdapter
) : ViewModel() {

    private val _uuid = UUID.fromString("4a202ae9-4a5b-4ed9-9df5-31d3b58c3b88")
    private val _appName = "Bchat"

    private val _devicesConnected = MutableLiveData<MutableList<BluetoothDevice?>>(mutableListOf())
    val devicesToChat: LiveData<MutableList<BluetoothDevice?>> = _devicesConnected
    
    private val _chatDevices = MutableLiveData<MutableList<BluetoothDevice>?>()
    val chatDevices: MutableLiveData<MutableList<BluetoothDevice>?> = _chatDevices

    private val _socketList = MutableLiveData< MutableList<BluetoothSocket>>()

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: MutableLiveData<Boolean> = _isConnected

    private val _messageList = MutableLiveData<MutableList<BluetoothMessage>>(mutableListOf())
    val messageList: MutableLiveData<MutableList<BluetoothMessage>> = _messageList

    private var _outputStream: MutableMap<String, OutputStream?> = mutableMapOf()

    @SuppressLint("MissingPermission")
    fun addDeviceAndSocketToMemory(socket: BluetoothSocket){
        try {
            val currentSocketList = _socketList.value ?: mutableListOf()
            if(!currentSocketList.contains(socket)){
                currentSocketList.add(socket)
                _socketList.postValue(currentSocketList)
            }
            val currentDeviceList = _devicesConnected.value?.toMutableList() ?: mutableListOf()
            for (socket in currentSocketList) {
                val remoteDevice = socket.remoteDevice
                if(!currentDeviceList.contains(remoteDevice))
                    currentDeviceList.add(remoteDevice)
                _outputStream.putIfAbsent(remoteDevice.name.toString(), socket.outputStream)
            }
            _devicesConnected.postValue(currentDeviceList)
        }
        catch (e: IOException){
            Log.d("network_check", "something went wrong when adding device to the memory: ${e}")
        }
    }

    @SuppressLint("MissingPermission")
    fun removeDeviceAndSocketFromMemory(socket: BluetoothSocket){
        try {
            val currentSocketList = _socketList.value ?: mutableListOf()
            if(currentSocketList.contains(socket)){
                currentSocketList.remove(socket)
                _socketList.postValue(currentSocketList)
            }
            val currentDeviceList = _devicesConnected.value?.toMutableList() ?: mutableListOf()
            for (socket in currentSocketList) {
                val remoteDevice = socket.remoteDevice
                if(currentDeviceList.contains(remoteDevice))
                    currentDeviceList.remove(remoteDevice)
                _outputStream.remove(remoteDevice.name.toString(), socket.outputStream)
            }
            _devicesConnected.postValue(currentDeviceList)
            val chatDevices = _chatDevices.value ?: emptyList()
            if(!chatDevices.isEmpty()){
                if(chatDevices.contains(socket.remoteDevice))
                    _chatDevices.postValue(null)
            }
            socket.close()
        }
        catch (e: IOException){
            Log.d("network_check", "something went wrong when removing device from the memory: ${e}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(){
        if(_bluetoothAdapter == null){
            Log.d("network_check", "bluetooth variables undefined, returning")
            return
        }
        Thread{
            try {
                val running = true
                var isManaging = false
                while(running){
                    val bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(_appName, _uuid)
                    Log.d("network_check", "the socket is open, waiting for a connection...")
                    val socket = bluetoothServerSocket.accept()
                    if(socket != null){
                        Log.d("network_check", "a device connected")
                        addDeviceAndSocketToMemory(socket)
                        if(!isManaging){
                            manageConnectedSocket()
                            isManaging = true
                        }
                    }
                }
            }
            catch (e: IOException){
                Log.d("network_check", "socket error: ${e.message}")
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun manageConnectedSocket(){
        Thread{
            val buffer = ByteArray(1024)
            var bytes: Int
            while(true){
                val sockets = _socketList.value?.toList() ?: emptyList()
                if(sockets.isNotEmpty()) {
                    updateIsConnected(true)
                    for (socket in sockets) {
                        try {
                            val inputStream = socket.inputStream
                            bytes = inputStream.read(buffer)
                            val incomingMessage = String(buffer, 0, bytes)
                            val remoteDevice = socket.remoteDevice
                            val otherDevicesInTheChat = _chatDevices.value?.filter { device -> device.address != remoteDevice.address } ?: emptyList()
                            forwardTheMessageToTheOtherDevices(incomingMessage, otherDevicesInTheChat)
                            val item = BluetoothMessage(1, remoteDevice.address, incomingMessage, System.currentTimeMillis().toString(), remoteDevice.name)
                            val currentList = _messageList.value?.toMutableList() ?: mutableListOf()
                            currentList.add(item)
                            _messageList.postValue(currentList)
                        } catch (e: IOException) {
                            Log.d("chat_check", "Connection lost: ${e.message}")
                            removeDeviceAndSocketFromMemory(socket)
                            updateIsConnected(false)
                            break
                        }
                    }
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
                addDeviceAndSocketToMemory(socket)
                Log.d("network_check", "attempting to connnect: ${device.name}")
                Log.d("network_check", "connection is successful: ${device.name}")
                updateIsConnected(true)
                manageConnectedSocket()
            }
            catch (e: IOException){
                Log.d("network_check", "client socket error: ${e.message}")
                try {
                    removeDeviceAndSocketFromMemory(socket)
                    updateIsConnected(false)
                }
                catch (closeException: IOException) {
                    Log.e("network_check", "socket couldn't be closed, ", closeException)
                    updateIsConnected(false)
                }
            }
        }.start()
    }
    
    @SuppressLint("MissingPermission")
    fun sendMessage(message: String){
        if(_outputStream.isEmpty()) {
            Log.d("network_check_send", "output stream is null, returning")
            return
        }
        Thread{
            try {
                val devices = _chatDevices.value ?: emptyList()
                if(devices.isEmpty())
                    return@Thread
                val byte = message.toByteArray(Charsets.UTF_8)
                for(device in devices){
                    Log.d("network_check_send", "attempting to send via outputstreams")
                    val outputStream = _outputStream[device.name.toString()]
                    if(outputStream != null){
                        outputStream.write(byte)
                        outputStream.flush()
                    }
                    else
                        Log.d("network_check_send", "outputstream is null, failed to flush")
                }                
                val currentList = _messageList.value?.toMutableList() ?: mutableListOf()
                val item = BluetoothMessage(message = message, timestamp = System.currentTimeMillis().toString(), isSentByMe = true)
                currentList.add(item)
                _messageList.postValue(currentList)
                Log.d("network_check", "flushed")
            }
            catch (e: IOException) {
                Log.e("chat_check", "Something went wrong sending the message: ${e.message}")
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun forwardTheMessageToTheOtherDevices(message: String, otherDevices: List<BluetoothDevice>){
        if(otherDevices.isNotEmpty()){
            try {
                for (otherDevice in otherDevices){
                    val bytes = message.toByteArray()
                    val outputStream = _outputStream.getValue(otherDevice.name.toString())
                    if(outputStream == null){
                        Log.d("network_check", "the outputstream is null, message couldn't be forwarded")
                        return
                    }
                    outputStream.write(bytes)
                    outputStream.flush()
                }
            }
            catch (e: IOException){
                Log.d("network_check", "something went wrong when forwarding: $e")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun cutTheConnection(device: BluetoothDevice){
        val currentList = _socketList.value?.toMutableList() ?: mutableListOf()
        for (socket in currentList){
            if(device.name.toString() == socket.remoteDevice.name.toString())
                removeDeviceAndSocketFromMemory(socket)
        }
    }

    fun updateChatDevices(device: List<BluetoothDevice>){
        _chatDevices.postValue(device.toMutableList())
    }

    fun updateIsConnected(b: Boolean){
        if(!b)
            Log.e("chat_check", "Something went wrong with the connection")
        _isConnected.postValue(b)
    }

    override fun onCleared() {
        super.onCleared()
        _chatDevices.postValue(null)
    }

}