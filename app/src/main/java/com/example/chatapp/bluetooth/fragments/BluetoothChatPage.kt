package com.example.chatapp.bluetooth.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.bluetooth.adapters.BluetoothChatMessageAdapter
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import com.example.chatapp.databinding.BFragmentChatPageBinding
import com.example.chatapp.bluetooth.viewmodel.BluetoothMessagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BluetoothChatPage : Fragment() {

    private lateinit var binding: BFragmentChatPageBinding
    private val bluetoothChatViewModel: BluetoothMessagingViewModel by activityViewModels()
    private var _chatDevice : BluetoothDeviceListItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BFragmentChatPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = BluetoothChatMessageAdapter()
        binding.bChatMessageRV.adapter = adapter
        binding.bChatMessageRV.layoutManager = layoutManager

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bluetoothChatViewModel.chatDevice.collect { device ->
                    Log.d("scan_assessment", "chatdevice updating in chatpage: ${device}")
                    binding.bChattingUserNick.text = device?.nick
                    _chatDevice = device
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bluetoothChatViewModel.messageList.collect { messages ->
                    Log.d("connection_assessment", "chatpage - chathistory: $messages")
                    adapter.submitList(messages.sortedBy { it.timestamp })
                    binding.bChatMessageRV.scrollToPosition(adapter.currentList.size - 1)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bluetoothChatViewModel.devicesConnected.collect { connectedDevices ->
                    if(connectedDevices.none { it?.address == _chatDevice?.macAddress })
                        findNavController().popBackStack()
                }
            }
        }

        binding.endConnectionButton.setOnClickListener {
            bluetoothChatViewModel.onEvent(GeneralBluetoothEvent.EndTheConnection(_chatDevice))
            findNavController().popBackStack()
        }

        binding.bSendMessageButton.setOnClickListener {
            val message = binding.bSendMessageTextField.text.toString()
            val device = _chatDevice
            Log.d("scan_assessment", "chat device: $device, message: $message")
            if(message.isBlank() || device == null) return@setOnClickListener
            val bldevice = bluetoothChatViewModel.getRemoteDevice(device.macAddress)
            bldevice?.let {
                bluetoothChatViewModel.onEvent(GeneralBluetoothEvent.SendMessage(message, it))
                binding.bSendMessageTextField.text.clear()
            }
        }

    }

}