package com.example.chatapp.scan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.scan.data.entity.ScanResultObject
import com.example.chatapp.scan.data.repo.NetworkScanRepo
import com.example.chatapp.scan.data.state.NetworkScanState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkScanViewModel @Inject constructor(private val repo: NetworkScanRepo): ViewModel() {

    private val TAG = "NetworkScanViewModel"

    private val _state = MutableStateFlow(NetworkScanState())
    val state = _state.asStateFlow()

    private val _deviceList = MutableStateFlow<List<ScanResultObject>>(emptyList())
    val deviceList: StateFlow<List<ScanResultObject>> = _deviceList.asStateFlow()

    private var scanJob: Job? = null

    fun onEvent(event: NetworkEvent){
        Log.d(TAG, "onEvent received: $event")
        when(event){
            NetworkEvent.PerformScan -> {
                Log.i(TAG, "onEvent: Starting network scan flow")
                _state.value = _state.value.copy(isScanning = true)
                startScanning()
            }
            NetworkEvent.StopScan -> {
                Log.i(TAG, "onEvent: Stopping network scan flow")
                _state.value = _state.value.copy(isScanning = false)
                stopScanning()
            }
        }
    }

    private fun startScanning() {
        scanJob?.cancel()
        _deviceList.value = emptyList()
        scanJob = viewModelScope.launch {
            repo.scanNetwork().collect { newDevice ->
                val currentList = _deviceList.value
                if (currentList.none { it.ipAddress == newDevice.ipAddress }) {
                    val updatedList = currentList + newDevice
                    Log.d(TAG, "startScanning: Found new device $newDevice. Total list size: ${updatedList.size}")
                    _deviceList.value = updatedList
                }
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        Log.i(TAG, "stopScanning: Scanning job cancelled. Scan results preserved: ${_deviceList.value.size} devices.")
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}