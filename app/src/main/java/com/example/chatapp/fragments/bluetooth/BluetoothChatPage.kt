package com.example.chatapp.fragments.bluetooth

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.adapters.BluetoothChatMessageAdapter
import com.example.chatapp.databinding.BFragmentChatPageBinding
import com.example.chatapp.util.createBluetoothItem
import com.example.chatapp.viewmodels.BluetoothModule.BluetoothMessagingViewModel

class BluetoothChatPage : Fragment() {

    private lateinit var binding: BFragmentChatPageBinding
    private val bluetoothChatViewModel: BluetoothMessagingViewModel by activityViewModels()

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
        bluetoothChatViewModel.devicesToChat.observe(viewLifecycleOwner) { users ->
            var groupChatName = ""
            for(user in users){
                if (user != null) {
                    Log.d("network_check_chat_page", "Device connected: ${user.name}")
                    groupChatName += user.name + ", "
                } else {
                    Log.d("network_check_chat_page", "Waiting for device data...")
                }
            }
            binding.bChattingUserNick.text = groupChatName
        }

        var devicesToChat = listOf<BluetoothDevice>()
        bluetoothChatViewModel.chatDevices.observe(viewLifecycleOwner) { chatDevices ->
            devicesToChat = chatDevices?.toList() ?: emptyList()
        }

        binding.endConnectionButton.setOnClickListener {
            if(devicesToChat.isNotEmpty()) {
                for (device in devicesToChat) {
                    bluetoothChatViewModel.cutTheConnection(device)
                }
            }
        }

        bluetoothChatViewModel.messageList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.bSendMessageButton.setOnClickListener {
            val text = binding.bSendMessageTextField.text.toString()
            if (text.isNotEmpty()) {
                bluetoothChatViewModel.sendMessage(text)
                binding.bSendMessageTextField.setText("")
            }
        }

    }

}