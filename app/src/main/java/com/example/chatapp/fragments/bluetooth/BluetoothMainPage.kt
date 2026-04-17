package com.example.chatapp.fragments.bluetooth

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.BluetoothUserListAdapter
import com.example.chatapp.data.entity.BluetoothDeviceListItem
import com.example.chatapp.data.entity.ObjectConstants
import com.example.chatapp.databinding.BFragmentMainPageBinding
import com.example.chatapp.util.fetchTheAnswer
import com.example.chatapp.util.howManyRefused
import com.example.chatapp.util.saveTheAnswer
import android.os.Build
import androidx.annotation.RequiresPermission


class BluetoothMainPage : Fragment() {

    private lateinit var binding: BFragmentMainPageBinding

    private val PERM_ANSWER_KEY = ObjectConstants.PERM_ANSWER_KEY
    private val DONT_ASK_AGAIN_KEY = ObjectConstants.DONT_ASK_AGAIN_KEY
    private val NOT_SEEN_DIALOGUE = ObjectConstants.NOT_SEEN_DIALOGUE
    private val SEEN_DIALOGUE_REFUSE = ObjectConstants.SEEN_DIALOGUE_REFUSE
    private val SEEN_DIALOGUE_ACCEPT = ObjectConstants.SEEN_DIALOGUE_ACCEPT

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


    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private val SCAN_DURATION: Long = 10000
    private val CustomLeScanCallback: ScanCallback = object: ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            Log.d("scan_results_check", "Scan failed: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("scan_results_check", "A result is found: $result")
            val currentList = bluetoothDeviceListAdapter.currentList
            if(result == null){
                Log.d("scan_results_check", "result is null, returning")
                return
            }
            val device = result.device
            if(device != null) {
                Log.d("scan_results_check", "A device is not null, device: $device")
                val item = if (areAllPermissionsGranted()) {
                    Log.d("scan_results_check", "Permissions are not granted, checking permission and returning")
                    checkPermission(permissions)
                    return
                }
                else{
                    BluetoothDeviceListItem(deviceName = device.name ?: "undefined", macAddress = device.address, deviceType =  device.type, listOfUUIDs = device.uuids, lastMessageDate = "", lastMessage = "", lastMessageStatus = 1, howManyUnseen = 0, bluetoothClass = device.bluetoothClass)
                }
                Log.d("scan_results_check", "Created an item: $item")
                currentList.add(item)
                Log.d("scan_results_check", "Adding the item to the list: $currentList")
                bluetoothDeviceListAdapter.submitList(currentList)
                bluetoothDeviceListAdapter.notifyDataSetChanged()
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

    private val bluetoothDeviceListAdapter = BluetoothUserListAdapter(requireContext())

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        permissions.entries.forEach { permission ->
            Log.d("permission_check", "${permission.key} is granted: ${permission.value}")
        }
        if(permissions.entries.any { !it.value }) {
            savePermissionsAsRefused()
            displayButtons()
            Log.d("answer_check", "answer should be $SEEN_DIALOGUE_REFUSE")
            Log.d("answer_check", "and answer is ${fetchTheAnswer(PERM_ANSWER_KEY, requireContext())}")
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = BFragmentMainPageBinding.inflate(inflater)
        val answer = fetchTheAnswer(PERM_ANSWER_KEY, requireContext())
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val devicesRecyclerView = binding.bUserList
        devicesRecyclerView.layoutManager = layoutManager
        devicesRecyclerView.adapter = bluetoothDeviceListAdapter

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

        return binding.root
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(){
        if(areAllPermissionsGranted()){
            defineBluetoothVariables()
            val pairedDevices = bluetoothAdapter?.bondedDevices
            val pairedList = ArrayList<BluetoothDeviceListItem>()
            if(pairedDevices != null){
                val filteredList = pairedDevices.filter { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.PHONE || it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER }
                filteredList.forEach { device ->
                    val item = BluetoothDeviceListItem(deviceName = device.name ?: "undefined", macAddress = device.address, deviceType =  device.type, listOfUUIDs = device.uuids, lastMessageDate = "", lastMessage = "", lastMessageStatus = 1, howManyUnseen = 0, bluetoothClass = device.bluetoothClass)
                    pairedList.add(item)
                }
            }
            if(!pairedList.isNullOrEmpty())
                bluetoothDeviceListAdapter.submitList(pairedList)
        }
    }

    @SuppressLint("MissingPermission")
    fun scanDevices(){
        if(isAdded){
            Log.d("scan_results_check", "The fragment is attached, initiating")
            val animation = binding.scanDevicesButton
            val progressBar = binding.barToMove
            if(!areAllPermissionsGranted()){
                Toast.makeText(requireContext(), "This operation can not be launched since the required permissions are not granted.",
                    Toast.LENGTH_SHORT).show()
                Log.d("scan_results_check", "Permissions are not granted, returning")
                return
            }
            else{
                val filters = mutableListOf<ScanFilter>()
                val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                Log.d("scan_results_check", "Scanner object: $bluetoothLeScanner")
                if(!scanning){
                    Log.d("scan_results_check", "Scan will begin")
                    progressBar.x = 0f
                    animation.progress = 0f
                    if(!animation.isAnimating){
                        animation.playAnimation()
                    }
                    progressBar.animate().setDuration(SCAN_DURATION).translationX(progressBar.width.toFloat()).withEndAction {
                        animation.cancelAnimation()
                        scanning = false
                        bluetoothLeScanner!!.stopScan(CustomLeScanCallback)
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

    fun handlePostRequest(context: Context){
        Toast.makeText(context, "This module needs specific permissions to operate. You can continue experiencing the rest of the app irrelevant to the permissions.", Toast.LENGTH_LONG).show()
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

    fun displayButtons(){
        /*val dontAskAgain = fetchTheAnswer(DONT_ASK_AGAIN_KEY, requireContext())
        Log.d("answer_check", "dontAskAgain $dontAskAgain")*/
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

    fun checkBackgroundLocationPermission(): Boolean{
        return requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isAnyPermissionDenied(): Boolean{
        return permissions.any { permission -> requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED }
    }

    fun areAllPermissionsGranted() : Boolean{
        return !isAnyPermissionDenied()
        /*if(!isAndroid11OrLower()){
            return !isAnyPermissionDenied()
        }
        else{
            return !isAnyPermissionDenied() && checkBackgroundLocationPermission()
        }*/
    }

    fun isAndroid11OrLower(): Boolean{
        return Build.VERSION.SDK_INT <= 30
    }

    fun savePermissionsAsAccepted(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_ACCEPT)
    }

    fun savePermissionsAsRefused(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_REFUSE)
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT])
    fun postSuccessfulPermissionRequestSchedule(){
        savePermissionsAsAccepted()
        hideButtons()
        getPairedDevices()
        defineBluetoothVariables()
    }

}