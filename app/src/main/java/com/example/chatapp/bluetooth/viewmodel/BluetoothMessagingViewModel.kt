package com.example.chatapp.bluetooth.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.data.repo.client.ClientHandlerImp
import com.example.chatapp.bluetooth.data.repo.general.GeneralHandlerImp
import com.example.chatapp.bluetooth.event.BluetoothEvent
import com.example.chatapp.bluetooth.event.ClientBluetoothEvent
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import com.example.chatapp.bluetooth.util.transformToMD5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import java.io.OutputStream
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BluetoothMessagingViewModel @Inject constructor(
    private val repo: BluetoothRepo,
    private val generalHandler: GeneralHandlerImp,
    private val clientHandler: ClientHandlerImp
) : ViewModel() {

    private val _devicesConnected = MutableStateFlow<MutableList<BluetoothDevice?>>(mutableListOf())
    val devicesConnected: StateFlow<MutableList<BluetoothDevice?>> = _devicesConnected

    private var _messageList = MutableStateFlow<List<BluetoothMessage>>(mutableListOf())
    val messageList: MutableStateFlow<List<BluetoothMessage>> = _messageList

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _chatDevice : MutableStateFlow<BluetoothDeviceListItem?> = MutableStateFlow(null)
    val chatDevice : StateFlow<BluetoothDeviceListItem?> = _chatDevice

    private val _scanResults = MutableStateFlow<List<BluetoothDeviceListItem>>(mutableListOf())
    val scanResults : StateFlow<List<BluetoothDeviceListItem>> = _scanResults

    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val errors = MutableStateFlow<String>("")

    init {
        viewModelScope.launch {
            repo.isScanning.collect { isScanningRepo ->
                _isScanning.value = isScanningRepo
            }
        }
        viewModelScope.launch {
            repo.chatHistory.collect { messages ->
                Log.d("connection_assessment", "taptochat - chathistory: $messages")
                _messageList.value = messages
            }
        }
        viewModelScope.launch {
            _isConnected.value = false

            repo.connectedDevices.collect { devices ->
                _devicesConnected.value = devices.toMutableList()
                _isConnected.value = devices.isNotEmpty()
            }
        }
        viewModelScope.launch {
            repo.chatDevice.collect { device ->
                Log.d("scan_assessment", "chatdevice updating in vm: ${_chatDevice.value}")
                _chatDevice.value = device
            }
        }
        viewModelScope.launch {
            Log.d("scan_assessment", "observing viewmodel scanresult")
            repo.scanResults.collect { devices ->
                Log.d("scan_assessment", "observing viewmodel scanresult: $devices")
                _scanResults.value = devices
            }
        }
    }

    fun onEvent(event: BluetoothEvent){
        when(event){
            is GeneralBluetoothEvent -> {
                viewModelScope.launch {
                    generalHandler.handleGeneralEvents(event)
                }
            }
            is ClientBluetoothEvent -> {
                viewModelScope.launch {
                    clientHandler.handleClientEvent(event)
                }
            }
        }
    }

    fun getRemoteDevice(mac: String): BluetoothDevice?{
        return repo.getRemoteDevice(mac)
    }


    override fun onCleared() {
        super.onCleared()
        //_chatDevices.postValue(null)
    }

}