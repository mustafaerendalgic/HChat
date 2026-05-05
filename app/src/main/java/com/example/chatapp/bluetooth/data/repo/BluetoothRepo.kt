package com.example.chatapp.bluetooth.data.repo

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.chatapp.bluetooth.data.entity.BluetoothConnection
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import com.example.chatapp.bluetooth.room.BluetoothDao
import com.example.chatapp.bluetooth.util.createBluetoothItem
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import com.example.chatapp.bluetooth.util.transformToMD5
import com.google.android.play.integrity.internal.b
import com.google.common.primitives.Chars
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okio.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.collections.sorted
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class BluetoothRepo @Inject constructor(private val dao: BluetoothDao, val bluetoothAdapter: BluetoothAdapter) {

    private var _scanResults = MutableStateFlow<MutableList<BluetoothDeviceListItem>>(mutableListOf())
    val scanResults: StateFlow<MutableList<BluetoothDeviceListItem>> = _scanResults

    private val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
    private val leScanner = bluetoothAdapter.bluetoothLeScanner

    private val _chatFileName = MutableStateFlow<String>("")

    private var _chatHistory : Flow<List<BluetoothMessage>> = _chatFileName.flatMapLatest { fileName ->
        if(fileName == null) flowOf(emptyList())
        else dao.getTheChatHistory(fileName)
    }

    val chatHistory: Flow<List<BluetoothMessage>> = _chatHistory

    private var _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices

    private var _connectionList = MutableStateFlow<List<BluetoothConnection>>(emptyList())
    val connectionList : StateFlow<List<BluetoothConnection>> = _connectionList

    private var _errors = MutableSharedFlow<GeneralBluetoothEvent.Error>()
    val errors = _errors.asSharedFlow()

    private var lastMessageID = androidx.room.concurrent.AtomicInt(0)

    private var _name = MutableStateFlow<String>("")

    private var _deviceRole = AtomicInteger(ObjectConstants.IDLE_CODE)

    private val _chatDevice : MutableStateFlow<BluetoothDeviceListItem?> = MutableStateFlow(null)
    val chatDevice : StateFlow<BluetoothDeviceListItem?> = _chatDevice

    private val ManageScanning = ManageHostingAndConnecting()

    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    private fun setIsScanning(boolean: Boolean) {_isScanning.value = boolean}

    suspend fun upsertMessage(message: BluetoothMessage) = dao.updateMessage(message)

    private val AdvertiseCallback = object: AdvertiseCallback(){
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("scan_assessment", "advertiseCallback - failure, $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("scan_assessment", "advertiseCallback - success")
        }

    }

    fun getRemoteDevice(mac: String): BluetoothDevice?{
        return bluetoothAdapter.getRemoteDevice(mac)
    }

    fun setName(name: String){
        _name.value = name
    }

    suspend fun tapToChat(device: BluetoothDeviceListItem){
        if(_connectionList.value.any { it.device.address != device.macAddress }) {
            Log.d("connection_assessment", "taptochat - device is not connected, returning: ${_connectionList.value}")
            return
        }
        _chatDevice.value = device
        Log.d("connection_assessment", "taptochat - chatdevice updating in repo: ${_chatDevice.value}")
        _chatFileName.value = transformToMD5(device.macAddress)
        lastMessageID.set(dao.getTheLastSentMessageID(_chatFileName.value) ?: 0)
    }

    private suspend fun emitError(message: String){
        _errors.emit(GeneralBluetoothEvent.Error(message))
    }

    @SuppressLint("MissingPermission")
    private val ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scan_assessment", "scanCallback - failure")
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
            Log.d("scan_assessment", "batchScanResults are found")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("scan_assessment", "scanCallback - some results found")
            super.onScanResult(callbackType, result)
            if(result == null){
                Log.d("scan_assessment", "result is null, returning")
                return
            }
            val device = result.device
            val serviceDataMap = result.scanRecord?.serviceData ?: emptyMap()
            Log.d("scan_assessment", "onscanresult - scanRecord: ${result.scanRecord}, serviceData: ${result.scanRecord?.serviceData}, serviceDataMap: $serviceDataMap")
            val nickBytes = serviceDataMap[ObjectConstants.PARCEL_UUID]
            Log.d("scan_assessment", "scanCallback - nickbytes: $nickBytes, device: $device")
            if(nickBytes != null && device != null){
                val currentList = _scanResults.value
                val nick = String(nickBytes)
                Log.d("scan_assessment", "A device is not null, device: $device")
                val item = createBluetoothItem(device, nick)
                Log.d("scan_assessment", "Created an item: $item")
                if(!currentList.any { it.macAddress == item.macAddress }){
                    Log.d("scan_assessment", "Adding the item to the list: $currentList")
                    val newList = currentList.toMutableList().apply {
                        add(item)
                    }
                    Log.d("scan_assessment", "newlist: $newList")
                    _scanResults.value = newList
                    Log.d("scan_assessment", "scanResults value: ${_scanResults.value}")
                }
            }
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun setDeviceRole(role: Int){
        _deviceRole.set(role)
    }

    @SuppressLint("MissingPermission")
    suspend fun performScan(){
        Log.d("scan_assessment", "starting to scan")
        val name = _name.value.toByteArray()
        Log.d("scan_assessment", "performscan - nick: $name, nicksize: ${name.size}, parcelUUID: ${ObjectConstants.PARCEL_UUID}")
        val advertiseData = AdvertiseData.Builder().addServiceData(ObjectConstants.PARCEL_UUID, name).setIncludeDeviceName(true).build()
        when(_deviceRole.toInt()){
            ObjectConstants.CLINET_CODE -> {
                Log.d("scan_assessment", "device role is client")
                setIsScanning(true)
                ManageScanning.stopListeningAsServer()
                leScanner.startScan(ScanCallback)
                delay(10000)
                setIsScanning(false)
                leScanner.stopScan(ScanCallback)
            }
            ObjectConstants.SERVER_CODE -> {
                Log.d("scan_assessment", "device role is server")
                setIsScanning(true)
                advertiser.startAdvertising(AdvertiseSettings.Builder().build(), advertiseData, AdvertiseCallback)
                ManageScanning.hostDevicesAsServer()
                delay(10000)
                setIsScanning(false)
                ManageScanning.stopListeningAsServer()
                advertiser.stopAdvertising(AdvertiseCallback)
            }
            ObjectConstants.IDLE_CODE -> {
                Log.d("scan_assessment", "device role is idle")
                setIsScanning(true)
                advertiser.startAdvertising(AdvertiseSettings.Builder().build(), advertiseData, AdvertiseCallback)
                leScanner.startScan(ScanCallback)
                ManageScanning.hostDevicesAsServer()
                delay(10000)
                setIsScanning(false)
                ManageScanning.stopListeningAsServer()
                advertiser.stopAdvertising(AdvertiseCallback)
                leScanner.stopScan(ScanCallback)
            }
        }
    }

    suspend fun endConnection(deviceListItem: BluetoothDeviceListItem?){
        if(deviceListItem == null)
            return
        val device = bluetoothAdapter.getRemoteDevice(deviceListItem.macAddress)
        val connections = _connectionList.value
        val connection = connections.find { it.device == device }
        try {
            if (connection != null) {
                connection.socket.close()
                removeDeviceFromMemory(device)
                connection.scope.cancel()
            }
        }
        catch (e: Exception){
            emitError(e.message ?: "undefined error")
            Log.d("connection_assessment", "endConnection - Something went wrong when trying to end the connection: $e")
        }
    }

    private fun removeDeviceFromMemory(device: BluetoothDevice,){
        val connectionListTemp = _connectionList.value.toMutableList()
        val connectedDevicesTemp = _connectedDevices.value.toMutableList()
        connectionListTemp.removeIf { it.device == device }
        connectedDevicesTemp.removeIf { it == device }
        _connectionList.value = connectionListTemp
        _connectedDevices.value = connectedDevicesTemp
    }

    private fun saveDeviceToMemory(socket: BluetoothSocket, item: BluetoothConnection){
        val connectionListTemp = _connectionList.value.toMutableList()
        val connectedDevicesTemp = _connectedDevices.value.toMutableList()
        if(connectedDevicesTemp.none{it.address == socket.remoteDevice.address})
            connectedDevicesTemp.add(socket.remoteDevice)
        if(connectionListTemp.none { it.device == socket.remoteDevice }) {
            connectionListTemp.add(item)
        }
        _connectionList.value = connectionListTemp
        _connectedDevices.value = connectedDevicesTemp
    }

    fun getNextID(): Int{
        return lastMessageID.incrementAndGet()
    }

    suspend fun sendMessage(message: String, device: BluetoothDevice){
        try {
            val connectionList = _connectionList.value
            val connection = connectionList.find { it.device == device }
            if(connection == null)
                return
            connection.scope.launch {
                val messageBytes = message.toByteArray(Charsets.UTF_8)
                val parts = messageBytes.asList().chunked(1000)
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
                    val fullpacket = header + _name.value.toByteArray() + bytes.toByteArray()
                    connection.outputStream.write(fullpacket)
                    connection.outputStream.flush()
                }
                val item = BluetoothMessage(
                message = message,
                timestamp = System.currentTimeMillis().toString(),
                isSentByMe = true,
                messageID = getNextID(),
                chatFileName = _chatFileName.value,
                nickname = _name.value ?: "you")
                upsertMessage(item)
            }
        }
        catch (e: Exception){
            emitError(e.message ?: "something went wrong when sending message")
        }
    }

    @SuppressLint("MissingPermission")
    private fun organizeBytesIntoMessageItem(byteArrays: List<ByteArray>, connection: BluetoothConnection): BluetoothMessage{
        val sortedByteArray = byteArrays.sortedBy { it[4].toInt() and 0xFF}
        val messageID = getMessageID(byteArrays.last())
        val nickname = getNick(byteArrays.last())
        val sb = StringBuilder()
        sortedByteArray.forEach { byteArray ->
            sb.append(getMessage(byteArray))
        }
        val message = sb.toString()
        val item = BluetoothMessage(messageID, connection.device.address, message, System.currentTimeMillis().toString(), connection.device.name, nickname, false,transformToMD5(connection.device.address))
        return item
    }

    private fun getMessageID(packet: ByteArray): Int{
        return ByteBuffer.wrap(packet, 0, 2).short.toInt()
    }

    private fun getNick(packet: ByteArray): String{
        val nickSize = packet.get(3).toInt()
        val nickBytes = packet.copyOfRange(5, 5 + nickSize)
        val nick = String(nickBytes)
        return nick
    }

    private fun getMessage(packet: ByteArray): String{
        val nickSize = packet.get(3).toInt()
        return String(packet.copyOfRange(5 + nickSize, packet.size))
    }

    @SuppressLint("MissingPermission")
    private fun manageConnectedSocket(connection: BluetoothConnection){
        connection.scope.launch {
            try {
                Log.d("connection_assessment", "managing the connected socket")
                val buffer = ByteArray(ObjectConstants.BLUETOOTH_BUFFER_SIZE)
                var assemblyMap = mutableMapOf<Int, MutableList<ByteArray>>()
                while (true){
                    val byteSize = connection.socket.inputStream.read(buffer)
                    val byteArray = buffer.copyOfRange(0, byteSize)
                    val messageID = getMessageID(byteArray)
                    var lastPacketList = assemblyMap.getOrPut(messageID) { mutableListOf() }
                    lastPacketList.add(byteArray)
                    Log.d("connection_assessment", "manageConnectedSocket: totalParts: ${byteArray[2]}")
                    if(lastPacketList.size == byteArray[2].toInt()){
                        val item = organizeBytesIntoMessageItem(lastPacketList, connection)
                        Log.d("connection_assessment", "manageConnectedsocket: item is created: $item")
                        upsertMessage(item)
                        assemblyMap.remove(messageID)
                    }
                }
            }
            catch (e: Exception){
                Log.d("connection_assessment", "manageConnectedSocket - Something went wrong when managing: ${e.message}, causedby: ${e.cause}")
                connection.scope.cancel()
                removeDeviceFromMemory(connection.device)
                emitError(e.message ?: "undefined error")
            }
        }
    }

    inner class ManageHostingAndConnecting(){

        private var listener: BluetoothServerSocket? = null
        private var socketForEstablishing: BluetoothSocket? = null

        @SuppressLint("MissingPermission")
        fun hostDevicesAsServer(){
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    listener = bluetoothAdapter.listenUsingRfcommWithServiceRecord(ObjectConstants.appName, ObjectConstants._uuid)
                    while (true){
                        Log.d("connection_assessment", "hostDeviceAsServer - A connection is requested")
                        val socket = try {
                            listener?.accept()
                        }
                        catch (e: IOException){
                            Log.d("connection_assessment", "hostDeviceAsServer - something went wrong when assigning the socket: $e")
                            return@launch
                        }
                        if(_deviceRole.toInt() == ObjectConstants.CLINET_CODE){
                            socket?.close()
                            Log.d("connection_assessment", "hostDeviceAsServer - became client, discarding accepted socket")
                            return@launch
                        }
                        if(socket != null){
                            Log.d("connection_assessment", "hostDeviceAsServer - a client connected, will manage")
                            setDeviceRole(ObjectConstants.SERVER_CODE)
                            closeClientSocket()
                            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                            val connection = BluetoothConnection(socket, socket.inputStream, socket.outputStream, socket.remoteDevice, scope)
                            saveDeviceToMemory(socket, connection)
                            manageConnectedSocket(connection)
                            break
                        }
                    }
                }
                catch (e: IOException){
                    emitError("something went wrong when hosting, $e")
                    Log.d("connection_assessment", "hostDeviceAsServer - something went wrong: $e")
                    setDeviceRole(ObjectConstants.IDLE_CODE)
                } finally {
                    stopListeningAsServer()
                }
            }
        }

        @SuppressLint("MissingPermission")
        suspend fun establishConnectionAsClient(device: BluetoothDevice){
            setDeviceRole(ObjectConstants.CLINET_CODE)
            stopListeningAsServer()
            socketForEstablishing = device.createRfcommSocketToServiceRecord(ObjectConstants._uuid)
            val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            socketForEstablishing?.let { socket ->
                try {
                    Log.d("connection_assessment", "establish - Attempting to connect")
                    socket.connect()
                    if(_deviceRole.toInt() == ObjectConstants.SERVER_CODE){
                        Log.d("connection_assessment", "establish - the role is server, returning")
                        return@let
                    }
                    val item = BluetoothConnection(socket, socket.inputStream, socket.outputStream, socket.remoteDevice, coroutineScope)
                    saveDeviceToMemory(socket, item)
                    manageConnectedSocket(item)
                    bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(AdvertiseCallback)
                }
                catch (e: IOException){
                    emitError(e.message ?: "undefined error")
                    setDeviceRole(ObjectConstants.IDLE_CODE)
                    Log.d("connection_assessment", "establish - something went wrong: $e")
                }
            }
        }

        fun stopListeningAsServer(){
            listener?.close()
        }

        fun closeClientSocket(){
            socketForEstablishing?.close()
        }

    }

}