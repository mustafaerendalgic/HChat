package com.example.chatapp.bluetooth.data.repo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.DeviceRole
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.util.byteArrayToUuidString
import com.example.chatapp.bluetooth.util.convertUuidToByteArray
import com.example.chatapp.bluetooth.util.createBluetoothItem
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.collections.copyOfRange

class BleDiscoveryManager @Inject constructor(private val blManager: BluetoothManager, private val blAdapter: BluetoothAdapter) {

    private val _leScanner = blAdapter.bluetoothLeScanner
    private val _advertiser = blAdapter.bluetoothLeAdvertiser

    var onDeviceDiscovered: ((item: BluetoothDeviceListItem) -> Unit)? = null

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

    @SuppressLint("MissingPermission")
    private val ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scan_assessment", "scanCallback - failure")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if(result == null){
                Log.d("scan_assessment", "result is null, returning")
                return
            }
            val device = result.device
            val serviceDataMap = result.scanRecord?.serviceData ?: emptyMap()
            val combinedBytes = serviceDataMap[ObjectConstants.PARCEL_UUID]
            if(combinedBytes != null && combinedBytes.size >= 21){
                val uuidBytes = combinedBytes.copyOfRange(5, 21)
                val nickBytes = combinedBytes.copyOfRange(21, combinedBytes.size)
                val psmPortBytes = ByteBuffer.wrap(combinedBytes, 0, 4).int
                val roleBytes = combinedBytes[4].toInt()
                val uuid = byteArrayToUuidString(uuidBytes).trim()
                val nick = String(nickBytes, Charsets.UTF_8)
                val psmPort = psmPortBytes.toInt()
                Log.d("psm_assessment", "psm ${nick}: ${psmPort}")
                val role = roleBytes.toInt()
                val item = createBluetoothItem(device, nick, psmPort, role, uuid)
                onDeviceDiscovered?.invoke(item)
            }
        }
    }

    fun startAdvertising(psmPort: Int, deviceRole: Int, uuid: String, name: String){
        val nameBytes = name.toByteArray()
        val advertiseData = AdvertiseData.Builder().setIncludeDeviceName(true).build()
        val psmData = ByteBuffer.allocate(4).putInt(psmPort).array()
        val roleData: Byte = deviceRole.toByte()
        val uuidData = convertUuidToByteArray(uuid)
        val scanResponse = AdvertiseData.Builder().addServiceData(ObjectConstants.PARCEL_UUID, psmData + roleData + uuidData + nameBytes).build()
        _advertiser?.startAdvertising(AdvertiseSettings.Builder().build(), advertiseData, scanResponse, AdvertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising(){
        _advertiser?.stopAdvertising(AdvertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun startScanning(whatToDoWhenDeviceDetected: (item: BluetoothDeviceListItem) -> Unit){
        this.onDeviceDiscovered = whatToDoWhenDeviceDetected
        _leScanner?.startScan(ScanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning(){
        _leScanner?.stopScan(ScanCallback)
    }

}