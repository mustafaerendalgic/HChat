package com.example.chatapp.scan.data.repo

import android.net.wifi.WifiManager
import android.util.Log
import com.example.chatapp.scan.data.entity.ScanResultObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.InetSocketAddress
import javax.inject.Inject


class NetworkScanRepo @Inject constructor(private val wm: WifiManager) {

    private val TAG = "NetworkScanRepo"

    private fun getMacFromArpTable(ipAddress: String): String {
        Log.d(TAG, "getMacFromArpTable: Attempting to resolve MAC for IP: $ipAddress")
        try {
            val file = java.io.File("/proc/net/arp")
            if (file.exists()) {
                val reader = java.io.BufferedReader(java.io.FileReader(file))
                var line: String?
                reader.readLine()
                while (reader.readLine().also { line = it } != null) {
                    val splitted = line!!.split("\\s+".toRegex())
                    if (splitted.size >= 4 && ipAddress == splitted[0]) {
                        val mac = splitted[3]
                        if (mac.matches("..:..:..:..:..:..".toRegex()) && mac != "00:00:00:00:00:00") {
                            reader.close()
                            Log.d(TAG, "getMacFromArpTable: Resolved MAC: $mac for IP: $ipAddress")
                            return mac.uppercase()
                        }
                    }
                }
                reader.close()
                Log.d(TAG, "getMacFromArpTable: Finished reading ARP file, no match found for IP: $ipAddress")
            } else {
                Log.w(TAG, "getMacFromArpTable: /proc/net/arp file does not exist or is not readable (common on modern Android).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMacFromArpTable: Error reading ARP table", e)
        }
        return "Unavailable"
    }

    private fun getLocalIpAddress(): String? {
        Log.d(TAG, "getLocalIpAddress: Resolving local IP...")

        try {
            val ipInt = wm.connectionInfo.ipAddress
            if (ipInt != 0) {
                val strIp = String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
                Log.i(TAG, "getLocalIpAddress: Successfully resolved via WifiManager: $strIp")
                return strIp
            } else {
                Log.d(TAG, "getLocalIpAddress: WifiManager ipAddress is 0 (missing permissions or wifi disabled).")
            }
        } catch (e: Exception) {
            Log.w(TAG, "getLocalIpAddress: WifiManager failed to get IP: ${e.message}")
        }

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                for (intf in interfaces) {
                    val addrs = intf.inetAddresses
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (isIPv4 && (sAddr.startsWith("192.168.") || sAddr.startsWith("10.") || sAddr.startsWith("172."))) {
                                Log.i(TAG, "getLocalIpAddress: Successfully resolved via NetworkInterface fallback: $sAddr on interface ${intf.name}")
                                return sAddr
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getLocalIpAddress: NetworkInterface fallback failed", ex)
        }
        
        Log.e(TAG, "getLocalIpAddress: Could not resolve any local IP address!")
        return null
    }

    private fun isIpReachable(ip: String): Boolean {
        try {
            val process = Runtime.getRuntime().exec("ping -c 1 -w 1 $ip")
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.d(TAG, "isIpReachable: Ping SUCCESS for IP: $ip")
                return true
            }
        } catch (e: Exception) {
        }

        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, 80), 150)
            socket.close()
            Log.d(TAG, "isIpReachable: Socket connection SUCCESS on port 80 for IP: $ip")
            return true
        } catch (e: Exception) {}

        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, 443), 150)
            socket.close()
            Log.d(TAG, "isIpReachable: Socket connection SUCCESS on port 443 for IP: $ip")
            return true
        } catch (e: Exception) {}

        try {
            val inet = InetAddress.getByName(ip)
            if (inet.isReachable(200)) {
                Log.d(TAG, "isIpReachable: InetAddress.isReachable SUCCESS for IP: $ip")
                return true
            }
        } catch (e: Exception) {}

        return false
    }

    fun scanNetwork(): Flow<ScanResultObject> = channelFlow {
        Log.i(TAG, "scanNetwork: Starting network scanning process...")
        try {
            val strIp = getLocalIpAddress()
            if (strIp != null) {
                val ipParts = strIp.split(".")
                if (ipParts.size >= 4) {
                    val subnet = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}."
                    Log.i(TAG, "scanNetwork: Scanning subnet prefix: ${subnet}0 to ${subnet}255")
                    for (i in 0..255) {
                        launch(Dispatchers.IO) {
                            val ip = subnet + i
                            if (ip == strIp) {
                                val selfMac = getMacFromArpTable(ip)
                                Log.d(TAG, "scanNetwork: Found self device: $ip")
                                trySend(ScanResultObject(ip, "My Device (Host)", selfMac))
                                return@launch
                            }
                            try {
                                if (isIpReachable(ip)) {
                                    val name = InetAddress.getByName(ip)
                                    val hostName = if (name.hostName == ip) "Active Device" else name.hostName
                                    val mac = getMacFromArpTable(ip)
                                    Log.i(TAG, "scanNetwork: FOUND ACTIVE DEVICE - IP: $ip, Host: $hostName, MAC: $mac")
                                    trySend(ScanResultObject(ip, hostName, mac))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "scanNetwork: Error checking reachability for IP: $ip", e)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "scanNetwork: Invalid resolved local IP split format: $strIp")
                }
            } else {
                Log.e(TAG, "scanNetwork: local IP resolved as null, cannot start subnet scanning!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "scanNetwork: Exception during scanning loop start", e)
        }
        awaitClose { 
            Log.i(TAG, "scanNetwork: Scanning channel flow is closing/cancelled.")
        }
    }

}