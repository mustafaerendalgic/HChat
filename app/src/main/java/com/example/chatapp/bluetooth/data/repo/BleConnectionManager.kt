package com.example.chatapp.bluetooth.data.repo

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.chatapp.bluetooth.data.entity.BluetoothConnection
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import com.example.chatapp.bluetooth.data.entity.DeviceRole
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.util.byteArrayToUuidString
import com.example.chatapp.bluetooth.util.convertUuidToByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class BleConnectionManager @Inject constructor(private val bluetoothAdapter: BluetoothAdapter, private val bleMessageParser: BluetoothMessageParser) {

    var _listener: BluetoothServerSocket? = null
    private var _socketForEstablishing: BluetoothSocket? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    suspend fun startHostingAndGetPsm(): Int? = withContext(Dispatchers.IO) {
        try {
            _listener = bluetoothAdapter.listenUsingInsecureL2capChannel()
            _listener?.psm
        } catch (e: IOException) {
            Log.d("connection_assessment", "startHostingAndGetPsm - failed to open listener: $e")
            null
        }
    }

    suspend fun endConnection(connection: BluetoothConnection,
                              error: suspend (message: String) -> Unit,
                              onConnectionFound: suspend (uuid: String) -> Unit){
        try {
            connection.socket.close()
            onConnectionFound.invoke(connection.uuid)
            connection.scope.cancel()
        }
        catch (e: Exception){
            error(e.message ?: "undefined error")
            Log.d("connection_assessment", "endConnection - Something went wrong when trying to end the connection: $e")
        }
    }

    @SuppressLint("MissingPermission")
    fun manageConnectedSocket(connection: BluetoothConnection,
                                      socketError: suspend (message: String)  -> Unit,
                                      onMessageReceived: suspend (item: BluetoothMessage) -> Unit){
        connection.scope.launch {
            try {
                Log.d("connection_assessment", "managing the connected socket")
                val buffer = ByteArray(ObjectConstants.BLUETOOTH_BUFFER_SIZE)
                var assemblyMap = mutableMapOf<Int, MutableList<ByteArray>>()
                while (true){
                    val byteSize = connection.socket.inputStream.read(buffer)
                    val byteArray = buffer.copyOfRange(0, byteSize)
                    val messageID = bleMessageParser.getMessageID(byteArray)
                    if (byteSize == -1) {
                        throw IOException("connection closed by remote peer")
                    }
                    var lastPacketList = assemblyMap.getOrPut(messageID) { mutableListOf() }
                    lastPacketList.add(byteArray)
                    Log.d("connection_assessment", "manageConnectedSocket: totalParts: ${byteArray[2]}")
                    if(lastPacketList.size == byteArray[2].toInt()){
                        val item = bleMessageParser.organizeBytesIntoMessageItem(lastPacketList, connection)
                        Log.d("connection_assessment", "manageConnectedsocket: message item is created: $item")
                        onMessageReceived(item)
                    }
                }
            }
            catch (e: Exception){
                Log.d("connection_assessment", "manageConnectedSocket - Something went wrong when managing: ${e.message}, causedby: ${e.cause}")
                socketError(e.message ?: "undefined error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun acceptConnections(
        role: Int,
        error: suspend (message: String) -> Unit,
        connectionEstablishedAsHost: suspend (BluetoothConnection) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val psmPort = _listener?.psm ?: run {
                error("psm is null")
                return@launch
            }
            try {
                while (true) {
                    Log.d("connection_assessment", "acceptConnections - listening started")
                    val socket = try {
                        _listener?.accept()
                    } catch (e: IOException) {
                        error("${e.message}")
                        return@launch
                    }
                    if (role == DeviceRole.CLIENT) {
                        socket?.close()
                        throw IOException("became client, discarding accepted socket")
                    }
                    if (socket != null) {
                        val byte: Byte = -1
                        val outputStream = socket.outputStream
                        val inputStream = socket.inputStream
                        outputStream.write(byteArrayOf(byte))
                        outputStream.flush()
                        val buffer = ByteArray(16)
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead != 16) {
                            throw IOException("somethings wrong with the uuid")
                        }
                        val uuid = byteArrayToUuidString(buffer).trim().lowercase()
                        Log.d(
                            "connection_assessment",
                            "acceptConnections - a client connected, will manage"
                        )
                        closeClientSocket()
                        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                        val connection = BluetoothConnection(
                            socket,
                            socket.inputStream,
                            socket.outputStream,
                            socket.remoteDevice,
                            scope,
                            psmPort,
                            uuid
                        )
                        connectionEstablishedAsHost(connection)
                        break
                    }
                }
            } catch (e: IOException) {
                error("$e")
                Log.d("connection_assessment", "acceptConnections - something went wrong: $e")
            } finally {
                stopListeningAsServer()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun establishConnectionAsClient(device: BluetoothDeviceListItem,
                                    uuid: String,
                                    role: Int,
                                    error: suspend (message: String) -> Unit,
                                    connectionEstablishedAsClient: suspend (item: BluetoothConnection) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothAdapter.cancelDiscovery()
            stopListeningAsServer()
            val blDevice = device.device
            Log.d("psm_assessment", "establish, psm ${device.deviceUUID}: ${device.psm}")
            _socketForEstablishing = blDevice.createInsecureL2capChannel(device.psm)
            val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            _socketForEstablishing?.let { socket ->
                try {
                    Log.d("connection_assessment", "establish - Attempting to connect")
                    try {
                        socket.connect()
                    } catch (e: Exception) {
                        throw IOException("est. socket error: ${e.message}")
                    }
                    val inputStream = socket.inputStream
                    val outputStream = socket.outputStream
                    val buffer = ByteArray(1)
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead != 1) {
                        throw IOException("server byte request failed")
                    }
                    val uuidBytes = convertUuidToByteArray(uuid)
                    outputStream.write(uuidBytes)
                    outputStream.flush()
                    if (role == DeviceRole.SERVER) {
                        throw IOException("the role is server, returning")
                    }
                    Log.d("psm_assessment", device.psm.toString())
                    val item = BluetoothConnection(
                        socket,
                        socket.inputStream,
                        socket.outputStream,
                        socket.remoteDevice,
                        coroutineScope,
                        device.psm,
                        device.deviceUUID
                    )
                    connectionEstablishedAsClient(item)
                }
            catch (e: IOException){
                error(e.message.toString())}
            }
        }
    }

    fun stopListeningAsServer() {
        _listener?.close()
        _listener = null
    }

    fun closeClientSocket() {
        _socketForEstablishing?.close()
    }

}