package com.example.chatapp.bluetooth.data.repo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.chatapp.bluetooth.data.entity.BluetoothConnection
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import com.example.chatapp.bluetooth.data.entity.DeviceRole
import com.example.chatapp.bluetooth.room.BluetoothDao
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.data.repo.general.SPHandler
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okio.IOException
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import com.example.chatapp.bluetooth.util.getChatFileName


class BluetoothRepo @Inject constructor(private val dao: BluetoothDao,
                                        val bluetoothAdapter: BluetoothAdapter,
                                        private val spHandler: SPHandler,
                                        private val bluetoothMessageParser: BluetoothMessageParser,
                                        private val bleConnectionManager: BleConnectionManager,
                                        private val bleDiscoveryManager: BleDiscoveryManager
    ) {

    private var _scanResults = MutableStateFlow<MutableList<BluetoothDeviceListItem>>(mutableListOf())
    val scanResults: StateFlow<MutableList<BluetoothDeviceListItem>> = _scanResults

    private val _chatFileName = MutableStateFlow<String>("")

    private var _chatHistory : Flow<List<BluetoothMessage>> = _chatFileName.flatMapLatest { fileName ->
        if(fileName == null) flowOf(emptyList())
        else dao.getTheChatHistory(fileName)
    }

    val chatHistory: Flow<List<BluetoothMessage>> = _chatHistory

    private var _connectedDevices = MutableStateFlow<List<BluetoothDeviceListItem>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDeviceListItem>> = _connectedDevices

    private var _connectionList = MutableStateFlow<List<BluetoothConnection>>(emptyList())
    val connectionList : StateFlow<List<BluetoothConnection>> = _connectionList

    private var _errors = MutableSharedFlow<GeneralBluetoothEvent.Error>()
    val errors = _errors.asSharedFlow()

    private var lastMessageID = androidx.room.concurrent.AtomicInt(0)

    private var _name = MutableStateFlow<String>("")

    private val _deviceRoleFlow = MutableStateFlow<Int>(DeviceRole.IDLE)
    val deviceRole: StateFlow<Int> = _deviceRoleFlow.asStateFlow()

    private val _chatDevice : MutableStateFlow<BluetoothDeviceListItem?> = MutableStateFlow(null)
    val chatDevice : StateFlow<BluetoothDeviceListItem?> = _chatDevice

    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    private fun setIsScanning(boolean: Boolean) {_isScanning.value = boolean}

    suspend fun upsertMessage(message: BluetoothMessage) = dao.updateMessage(message)

    fun setName(name: String){
        _name.value = name
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun tapToChat(device: BluetoothDeviceListItem){
        if(_connectionList.value.none { it.uuid == device.deviceUUID }) {
            Log.d("connection_assessment", "taptochat - device is not connected, returning: ${_connectionList.value}")
            return
        }
        _chatDevice.value = device
        Log.d("connection_assessment", "taptochat - chatdevice updating in repo: ${_chatDevice.value}")
        _chatFileName.value = getChatFileName(device.deviceUUID)
        lastMessageID.set(dao.getTheLastSentMessageID(_chatFileName.value) ?: 0)
    }

    private suspend fun emitError(message: String){
        _errors.emit(GeneralBluetoothEvent.Error(message))
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun setDeviceRole(role: Int){
        _deviceRoleFlow.value = role
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun establishConnectionAsClient(device: BluetoothDeviceListItem){
        val uuid = spHandler.fetchUUIDRecord() ?: return
        val role = _deviceRoleFlow.value
        bleConnectionManager.establishConnectionAsClient(device, uuid, role, error = {emitError("couldn't establish the connection")} ) { connection ->
            saveDeviceToMemory(connection, DeviceRole.CLIENT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    suspend fun performScan(){
        _scanResults.value = mutableListOf<BluetoothDeviceListItem>()
        Log.d("scan_assessment", "starting to scan")
        val deviceRole = _deviceRoleFlow.value
        setIsScanning(true)
        val shouldScan = deviceRole == DeviceRole.IDLE || deviceRole == DeviceRole.CLIENT
        val shouldHost = deviceRole == DeviceRole.IDLE || deviceRole == DeviceRole.SERVER

        if(shouldScan){
            bleDiscoveryManager.startScanning { device ->
                val currentList = _scanResults.value.toMutableList()
                val index = currentList.indexOfFirst { it.deviceUUID.lowercase() == device.deviceUUID.lowercase()}
                if(index == -1) currentList.add(device)
                else currentList[index] = device
                _scanResults.value = currentList
            }
        }

        if(shouldHost){
            val psm = bleConnectionManager.startHostingAndGetPsm()
            bleConnectionManager.acceptConnections(_deviceRoleFlow.value, error = {emitError(it)}) { bleConnection ->
                saveDeviceToMemory(bleConnection, DeviceRole.SERVER)
            }
            val uuid = spHandler.fetchUUIDRecord()
            if(psm != null && uuid != null){
                bleDiscoveryManager.startAdvertising(psm, _deviceRoleFlow.value, uuid, _name.value)
            } else {
                emitError("psm veya uuid null, advertising başlatılamadı")
            }
        }

        delay(ObjectConstants.SCAN_TIME.toLong())
        setIsScanning(false)

        if(shouldScan){
            bleDiscoveryManager.stopScanning()
        }
        if(shouldHost){
            bleConnectionManager.stopListeningAsServer()
            bleDiscoveryManager.stopAdvertising()
        }
    }

    suspend fun endConnection(device: BluetoothDeviceListItem?){
        if(device == null) return
        val connection = _connectionList.value.find { it.uuid.lowercase() == device.deviceUUID } ?: return
        bleConnectionManager.endConnection(connection,  error = { message -> emitError(message) }) { connectionUuid ->
            removeDeviceFromMemory(connectionUuid)
        }
    }

    private suspend fun removeDeviceFromMemory(uuid: String){
        val connectionListTemp = _connectionList.value.toMutableList()
        val connectedDevicesTemp = _connectedDevices.value.toMutableList()
        val device = connectedDevicesTemp.find { it.deviceUUID == uuid }  ?: run {emitError("attempted to remove but device is already absent in memory"); return}
        connectionListTemp.removeIf { it.uuid == device.deviceUUID }
        connectedDevicesTemp.removeIf { it.deviceUUID == device.deviceUUID }
        _connectionList.value = connectionListTemp
        _connectedDevices.value = connectedDevicesTemp
        if(connectionListTemp.isEmpty() && connectedDevicesTemp.isEmpty()){
            setDeviceRole(DeviceRole.IDLE)
        }
    }

    private suspend fun saveDeviceToMemory(item: BluetoothConnection, ownerRole: Int = DeviceRole.SERVER){
        val connectionListTemp = _connectionList.value.toMutableList()
        val connectedDevicesTemp = _connectedDevices.value.toMutableList()
        val scanResults = _scanResults.value
        if(connectedDevicesTemp.none{it.deviceUUID == item.uuid}) {
            val deviceListItem = scanResults.find { it.deviceUUID.toString().lowercase().trim() == item.uuid.toString().lowercase().trim() } ?: run {
                emitError("selected device can't be recognized in scan results")
                item.socket.close()
                return
            }
            connectedDevicesTemp.add(deviceListItem)
        }
        if(connectionListTemp.none { it.uuid == item.uuid }) {
            connectionListTemp.add(item)
        }
        when(ownerRole){
            DeviceRole.CLIENT -> setDeviceRole(DeviceRole.CLIENT)
            DeviceRole.SERVER -> setDeviceRole(DeviceRole.SERVER)
        }
        _connectionList.value = connectionListTemp
        _connectedDevices.value = connectedDevicesTemp
        bleConnectionManager.manageConnectedSocket(item, socketError = {emitError(it); removeDeviceFromMemory(item.uuid)}){ message ->
            upsertMessage(message)
        }
    }

    fun getNextID(): Int{
        return lastMessageID.incrementAndGet()
    }

    suspend fun sendMessage(message: String, device: BluetoothDeviceListItem){
        try {
            val connectionList = _connectionList.value
            val connection = connectionList.find { it.uuid == device.deviceUUID }
            if(connection == null)
                return
            connection.scope.launch {
                val messageBytes = message.toByteArray(Charsets.UTF_8)
                val parts = messageBytes.asList().chunked(ObjectConstants.BLUETOOTH_BUFFER_SIZE)
                val totalParts = parts.size
                val currentID = getNextID()
                val nickByte = _name.value.toByteArray()
                val nickSize = nickByte.size
                parts.forEachIndexed { index, bytes ->
                    val header = ByteBuffer.allocate(5)
                        .putShort(currentID.toShort())
                        .put(totalParts.toByte())
                        .put(nickSize.toByte())
                        .put(index.toByte()).array()
                    val fullpacket = header + nickByte + bytes.toByteArray()
                    connection.outputStream.write(fullpacket)
                    connection.outputStream.flush()
                }
                val item = BluetoothMessage(
                message = message,
                timestamp = System.currentTimeMillis().toString(),
                isSentByMe = true,
                messageID = currentID,
                chatFileName = _chatFileName.value,
                nickname = _name.value)
                upsertMessage(item)
            }
        }
        catch (e: Exception){
            emitError(e.message ?: "something went wrong when sending message")
        }
    }

    suspend fun clearCache(){
        _scanResults.value = mutableListOf<BluetoothDeviceListItem>()
        val list = _connectedDevices.value
        list.forEach { connectedDevice ->
            val connection = _connectionList.value.find { connection -> connectedDevice.deviceUUID.lowercase() == connection.uuid.lowercase()}
            if(connection != null) {
                bleConnectionManager.endConnection(connection, error = {emitError(it)}) { uuid -> removeDeviceFromMemory(uuid) }

            }
        }
        _isScanning.value = false
        setDeviceRole(DeviceRole.IDLE)
    }

}