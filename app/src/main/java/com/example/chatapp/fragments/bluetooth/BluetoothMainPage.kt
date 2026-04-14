package com.example.chatapp.fragments.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import androidx.core.content.ContextCompat
import com.example.chatapp.adapters.BluetoothUserListAdapter
import com.example.chatapp.data.entity.ObjectConstants
import com.example.chatapp.databinding.BFragmentMainPageBinding
import com.example.chatapp.util.fetchTheAnswer
import com.example.chatapp.util.howManyRefused
import com.example.chatapp.util.saveTheAnswer
import com.google.android.gms.actions.NoteIntents


class BluetoothMainPage : Fragment() {

    private lateinit var binding: BFragmentMainPageBinding

    private val PERM_ANSWER_KEY = ObjectConstants.PERM_ANSWER_KEY
    private val DONT_ASK_AGAIN_KEY = ObjectConstants.DONT_ASK_AGAIN_KEY
    private val NOT_SEEN_DIALOGUE = ObjectConstants.NOT_SEEN_DIALOGUE
    private val SEEN_DIALOGUE_REFUSE = ObjectConstants.SEEN_DIALOGUE_REFUSE
    private val SEEN_DIALOGUE_ACCEPT = ObjectConstants.SEEN_DIALOGUE_ACCEPT

    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private var SCAN_DURATION: Long = 10000
    private val handler = Handler()

    private val bluetoothDeviceListAdapter = BluetoothUserListAdapter()

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
            savePermissionsAsAccepted()
            hideButtons()
            Log.d("answer_check", "answer should be $SEEN_DIALOGUE_ACCEPT")
            Log.d("answer_check", "and answer is ${fetchTheAnswer(PERM_ANSWER_KEY, requireContext())}")
        }
    }

    private val permissionNavigationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkPermission(permissions, requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = BFragmentMainPageBinding.inflate(inflater)
        val answer = fetchTheAnswer(PERM_ANSWER_KEY, requireContext())

        when(answer){
            NOT_SEEN_DIALOGUE -> {
                Log.d("answer_check", "answer is $answer")
                checkPermission(permissions, requireContext())
            }
            SEEN_DIALOGUE_ACCEPT -> {
                checkPermission(permissions, requireContext())
                hideButtons()
            }
            SEEN_DIALOGUE_REFUSE -> {
                checkPermission(permissions, requireContext())
                displayButtons()
            }
        }

        binding.requestPermissionsButton.setOnClickListener {
            val dontAskAgain = fetchTheAnswer(DONT_ASK_AGAIN_KEY, requireContext())
            Log.d("answer_check", "button clicked $dontAskAgain")
            if(dontAskAgain < 2)
                checkPermission(permissions, requireContext())
            else{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                permissionNavigationLauncher.launch(intent)
            }
        }

        return binding.root
    }

    fun scanDevices(){
        if(!scanning){
            handler.postDelayed({
                scanning = false,
                bluetoothLeScanner.stopScan()
            }, SCAN_DURATION)
        }
    }

    fun checkPermission(list: Array<String>, context: Context){
        val listTemp = ArrayList<String>()
        list.forEach { permission ->
            Log.d("permission_check_in_func", permission.toString())
            if(ContextCompat.checkSelfPermission(context, permission.toString()) == PackageManager.PERMISSION_DENIED) {
                listTemp.add(permission)
                Log.d("permission_check_in_func", "Permission wasn't granted, adding to the list: ${permission}, ${list}")
            }
        }
        if(listTemp.isNullOrEmpty()) {
            Log.d("permission_check_in_func", "all permissions are granted")
            hideButtons()
            savePermissionsAsAccepted()
        }
        else
            requestPermissionLauncher.launch(listTemp.toTypedArray())
    }

    fun handlePostRequest(context: Context){
        Toast.makeText(context, "This module needs specific permissions to operate. You can continue experiencing the rest of the app irrelevant to the permissions.", Toast.LENGTH_LONG).show()
    }

    fun hideButtons(){
        if(binding.requestPermissionsButton.isEnabled && binding.requestPermissionsButton.visibility == View.VISIBLE) {
            binding.requestPermissionsButton.visibility = View.GONE
            binding.requestPermissionsButton.isEnabled = false
            binding.askForPermissionAgainAnimation.visibility = View.GONE
        }
    }

    fun displayButtons(){
        /*val dontAskAgain = fetchTheAnswer(DONT_ASK_AGAIN_KEY, requireContext())
        Log.d("answer_check", "dontAskAgain $dontAskAgain")*/
        if(!binding.requestPermissionsButton.isEnabled && binding.requestPermissionsButton.visibility != View.VISIBLE) {
            binding.requestPermissionsButton.visibility = View.VISIBLE
            binding.requestPermissionsButton.isEnabled = true
            binding.askForPermissionAgainAnimation.visibility = View.VISIBLE
        }
    }

    fun savePermissionsAsAccepted(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_ACCEPT)
    }

    fun savePermissionsAsRefused(){
        saveTheAnswer(PERM_ANSWER_KEY, requireContext(), SEEN_DIALOGUE_REFUSE)
    }

}