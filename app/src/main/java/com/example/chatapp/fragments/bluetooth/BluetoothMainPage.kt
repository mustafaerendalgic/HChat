package com.example.chatapp.fragments.bluetooth

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.BluetoothUserListAdapter
import com.example.chatapp.data.entity.BluetoothDeviceListItem
import com.example.chatapp.data.entity.ObjectConstants
import com.example.chatapp.databinding.BFragmentMainPageBinding
import com.example.chatapp.util.fetchTheAnswer
import com.example.chatapp.util.howManyRefused
import com.example.chatapp.util.saveTheAnswer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.util.createBluetoothItem
import com.example.chatapp.viewmodels.BluetoothModule.BluetoothMessagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothMainPage : Fragment() {

    private lateinit var binding: BFragmentMainPageBinding

    private val PERM_ANSWER_KEY = ObjectConstants.PERM_ANSWER_KEY
    private val DONT_ASK_AGAIN_KEY = ObjectConstants.DONT_ASK_AGAIN_KEY
    private val NOT_SEEN_DIALOGUE = ObjectConstants.NOT_SEEN_DIALOGUE
    private val SEEN_DIALOGUE_REFUSE = ObjectConstants.SEEN_DIALOGUE_REFUSE
    private val SEEN_DIALOGUE_ACCEPT = ObjectConstants.SEEN_DIALOGUE_ACCEPT

    private val bluetoothChatViewModel: BluetoothMessagingViewModel by activityViewModels()

    @Inject lateinit var bluetoothManager: BluetoothManager
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private var devicesConnected = listOf<BluetoothDevice>()

    private var scanning = false
    private val SCAN_DURATION: Long = 10000
    private val CustomLeScanCallback: ScanCallback = object: ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            Log.d("scan_results_check", "Scan failed: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("scan_results_check", "A result is found: $result")
            val currentList = bluetoothDeviceListAdapter.currentList.toMutableList()
            if(result == null){
                Log.d("scan_results_check", "result is null, returning")
                return
            }
            val device = result.device
            if(device != null) {
                Log.d("scan_results_check", "A device is not null, device: $device")
                val item = if (!areAllPermissionsGranted()) {
                    Log.d("scan_results_check", "Permissions are not granted, checking permission and returning")
                    checkPermission(permissions)
                    return
                }
                else{
                    createBluetoothItem(device)
                }
                Log.d("scan_results_check", "Created an item: $item")
                if(!currentList.any { it.macAddress == item.macAddress })
                    currentList.add(item)
                Log.d("scan_results_check", "Adding the item to the list: $currentList")
                bluetoothDeviceListAdapter.submitList(currentList)
            }
            else{
                Log.d("scan_results_check", "No available device found")
                Toast.makeText(requireContext(), "No available device found", Toast.LENGTH_LONG).show()
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Log.d("scan_results_check", "Multiple results are found: $results")
            super.onBatchScanResults(results)
        }
    }

    val AdvertiseCallback : AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("advertise_check", "advertising has failed")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("advertise_check", "advertising performed successfully")
        }
    }

    private val bluetoothDeviceListAdapter = BluetoothUserListAdapter{devices -> handleDeviceClick(listOf(devices))}
    private val bluetoothDevicePairedListAdapter = BluetoothUserListAdapter{devices -> handleDeviceClick(listOf(devices))}

    private val permissions = if(!isAndroid11OrLower())arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )
    else{
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        permissions.entries.forEach { permission ->
            Log.d("permission_check", "${permission.key} is granted: ${permission.value}")
        }
        if(permissions.entries.any { !it.value }) {
            savePermissionsAsRefused()
            displayButtons()
            handlePostRequest(requireContext())
            howManyRefused(requireContext())
        }
        else{
            Log.d("permission_check", "all are granted, proceeding to ")
            if(isAndroid11OrLower()){
                handleBackgroundLocPermission()
            }
            else
                postSuccessfulPermissionRequestSchedule()
        }
    }

    @SuppressLint("MissingPermission")
    private val requestBackgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        if(checkBackgroundLocationPermission()){
            postSuccessfulPermissionRequestSchedule()
            Log.d("answer_check", "answer should be $SEEN_DIALOGUE_ACCEPT")
            Log.d("answer_check", "and answer is ${fetchTheAnswer(PERM_ANSWER_KEY, requireContext())}")
        }
    }

    fun handleBackgroundLocPermission(){
        requestBackgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private val permissionNavigationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkPermission(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defineBluetoothVariables()
    }

    override fun onAttach(context: Context) {
        defineBluetoothVariables()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothChatViewModel.devicesConnected.observe(viewLifecycleOwner) { devices ->
            devicesConnected = devices.mapNotNull { device -> device }
            val currentList = bluetoothDeviceListAdapter.currentList
            Log.d("network_check", "observing on onViewCreated - device check, current list: $currentList")
            val newList = currentList.map { currentDevice ->
                if(devices.any { it?.address == currentDevice.macAddress }){
                    Log.d("network_check", "device found as connected: $currentDevice")
                    currentDevice.isConnected = true
                }
                else{
                    Log.d("network_check", "device is not connected: $currentDevice")
                    currentDevice.isConnected = false
                }
                currentDevice
            }
            Log.d("network_check", "observing on onViewCreated - device check, new list: $newList")
            bluetoothDeviceListAdapter.submitList(newList)
            bluetoothDeviceListAdapter.notifyDataSetChanged()
        }

        defineBluetoothVariables()
        val answer = fetchTheAnswer(PERM_ANSWER_KEY, requireContext())
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val pairedLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val devicesRecyclerView = binding.bUserList
        val pairedDevicesRecyclerView = binding.bPairedUserList
        devicesRecyclerView.layoutManager = layoutManager
        pairedDevicesRecyclerView.layoutManager = pairedLayoutManager
        devicesRecyclerView.adapter = bluetoothDeviceListAdapter
        pairedDevicesRecyclerView.adapter = bluetoothDevicePairedListAdapter

        binding.scanDevicesButton.speed = 0.1f
        binding.scanDevicesButton.playAnimation()

        when(answer){
            NOT_SEEN_DIALOGUE -> {
                Log.d("answer_check", "answer is $answer")
                checkPermission(permissions)
            }
            SEEN_DIALOGUE_ACCEPT -> {
                checkPermission(permissions)
                hideButtons()
            }
            SEEN_DIALOGUE_REFUSE -> {
                checkPermission(permissions)
                displayButtons()
            }
        }

        binding.requestPermissionsButton.setOnClickListener {
            val dontAskAgain = fetchTheAnswer(DONT_ASK_AGAIN_KEY, requireContext())
            Log.d("answer_check", "button clicked $dontAskAgain")
            if(dontAskAgain < 2)
                checkPermission(permissions)
            else{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                permissionNavigationLauncher.launch(intent)
            }
        }

        binding.startScanText.setOnClickListener {
            defineBluetoothVariables()
            scanDevices()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BFragmentMainPageBinding.inflate(inflater)

        return binding.root
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(){
        if(areAllPermissionsGranted()){
            val pairedList = ArrayList<BluetoothDeviceListItem>()
            defineBluetoothVariables()
            val pairedDevices = bluetoothAdapter?.bondedDevices
            if(pairedDevices != null){
                val currentDevicesAddresses = bluetoothDevicePairedListAdapter.currentList.map { it.macAddress }
                val newDevicesAddresses = pairedDevices.map { it.address }
                if(currentDevicesAddresses.size == newDevicesAddresses.size && currentDevicesAddresses.containsAll(newDevicesAddresses))
                    return
                val filteredList = pairedDevices.filter { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.PHONE || it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER }
                filteredList.forEach { device ->
                    val item = createBluetoothItem(device)
                    pairedList.add(item)
                }
            }
            if(!pairedList.isNullOrEmpty()){
                bluetoothDevicePairedListAdapter.submitList(pairedList)
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun scanDevices(){
        if(isAdded){
            Log.d("scan_results_check", "The fragment is attached, initiating")
            val animation = binding.scanDevicesButton
            animation.speed = 1f
            val progressBar = binding.barToMove
            if(!areAllPermissionsGranted()){
                Toast.makeText(requireContext(), "This operation can not be launched since the required permissions are not granted.",
                    Toast.LENGTH_SHORT).show()
                Log.d("scan_results_check", "Permissions are not granted, returning")
                return
            }
            else{
                defineBluetoothVariables()
                makeDeviceDiscoverable()
                bluetoothChatViewModel.startServer()
                val filters = mutableListOf<ScanFilter>()
                val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                Log.d("scan_results_check", "Scanner object: $bluetoothLeScanner")
                if(!scanning){
                    Log.d("scan_results_check", "Scan will begin")
                    progressBar.x = 0f
                    if(!animation.isAnimating){
                        animation.playAnimation()
                    }
                    progressBar.animate().setDuration(SCAN_DURATION).translationX(progressBar.width.toFloat()).withEndAction {
                        animation.speed = 0.1f
                        scanning = false
                        bluetoothLeScanner!!.stopScan(CustomLeScanCallback)
                        bluetoothAdapter.cancelDiscovery()
                    }.start()
                }
                scanning = true
                Log.d("scan_results_check", "Scanner object again: $bluetoothLeScanner")
                bluetoothLeScanner!!.startScan(filters, settings, CustomLeScanCallback)

            }
        }
    }

    fun defineBluetoothVariables(){
        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager!!.adapter
        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner

        Log.d("permission_check_in_func", "all are defined, $bluetoothManager $bluetoothAdapter $bluetoothLeScanner")
    }

    @SuppressLint("MissingPermission")
    fun checkPermission(list: Array<String>){
        if(areAllPermissionsGranted()) {
            Log.d("permission_check_in_func", "all permissions are granted")
            postSuccessfulPermissionRequestSchedule()
        }
        else
            requestPermissionLauncher.launch(list)
    }

    fun isAnyPermissionDenied(): Boolean{
        return permissions.any { permission -> requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED }
    }

    fun areAllPermissionsGranted() : Boolean{
        if(!isAndroid11OrLower()){
            return !isAnyPermissionDenied()
        }
        else{
            return !isAnyPermissionDenied() && checkBackgroundLocationPermission()
        }
    }

    fun isAndroid11OrLower(): Boolean{
        return Build.VERSION.SDK_INT <= 30
    }

    fun displayButtons(){
        if(isAnyPermissionDenied()) {
            binding.requestPermissionsButton.visibility = View.VISIBLE
            binding.requestPermissionsButton.isEnabled = true
            binding.askForPermissionAgainAnimation.visibility = View.VISIBLE

            binding.scanDevicesButton.visibility = View.GONE
            binding.startScanText.visibility = View.GONE
            binding.startScanDecoration.visibility = View.GONE
            binding.originalProgressBar.visibility = View.GONE
            binding.barToMove.visibility = View.GONE
        }
    }

    fun handlePostRequest(context: Context){
        Toast.makeText(context, "This module needs specific permissions to operate. You can continue experiencing the rest of the app irrelevant to the permissions.", Toast.LENGTH_LONG).show()
    }

    fun handleDeviceClick(items: List<BluetoothDeviceListItem>){
        val deviceList = mutableListOf<BluetoothDevice>()
        if(items.all { it.isConnected }){
            Log.d("network_check_bmainpage", "handleDeviceClick - all items are connected, navigating")
            for (item in items){
                val device = bluetoothAdapter.getRemoteDevice(item.macAddress)
                deviceList.add(device)
            }
            val action = BluetoothMainPageDirections.bMainToChat()
            bluetoothChatViewModel.updateChatDevices(deviceList)
            findNavController().navigate(action)
        }
        else{
            for (item in items){
                if(item.isConnected == false) {
                    val device = bluetoothAdapter.getRemoteDevice(item.macAddress)
                    Log.d("network_check_bmainpage", "handleDeviceClick - attempting to connect to device: $device")
                    bluetoothChatViewModel.connectToDevice(device)
                }
            }
        }
    }

    fun hideButtons(){
        if(!isAnyPermissionDenied() || checkBackgroundLocationPermission()) {
            binding.requestPermissionsButton.visibility = View.GONE
            binding.requestPermissionsButton.isEnabled = false
            binding.askForPermissionAgainAnimation.visibility = View.GONE

            binding.scanDevicesButton.visibility = View.VISIBLE
            binding.startScanText.visibility = View.VISIBLE
            binding.startScanDecoration.visibility = View.VISIBLE
            binding.originalProgressBar.visibility = View.VISIBLE
            binding.barToMove.visibility = View.VISIBLE
        }
    }

    fun checkBackgroundLocationPermission(): Boolean{
        return requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    fun makeDeviceDiscoverable(){
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val advertiseSettings = AdvertiseSettings.Builder().build()
        val advertiseData = AdvertiseData.Builder().apply {
            setIncludeDeviceName(true)
        }.build()
        advertiser.startAdvertising(advertiseSettings, advertiseData, AdvertiseCallback)
    }

    fun savePermissionsAsAccepted(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_ACCEPT)
    }

    fun savePermissionsAsRefused(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_REFUSE)
    }

    @SuppressLint("MissingPermission")
    fun postSuccessfulPermissionRequestSchedule(){
        savePermissionsAsAccepted()
        hideButtons()
        getPairedDevices()
        defineBluetoothVariables()
    }

    @SuppressLint("MissingPermission")


    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(AdvertiseCallback)
        bluetoothAdapter.bluetoothLeScanner.stopScan(CustomLeScanCallback)
    }

}