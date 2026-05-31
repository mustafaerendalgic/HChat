package com.example.chatapp.scan.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.databinding.FragmentScanNetworkBinding
import com.example.chatapp.scan.adapters.NetworkScanAdapter
import com.example.chatapp.scan.viewmodel.NetworkEvent
import com.example.chatapp.scan.viewmodel.NetworkScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanNetworkFragment : Fragment() {

    private val TAG = "ScanNetworkFragment"
    private lateinit var binding: FragmentScanNetworkBinding
    private val viewModel: NetworkScanViewModel by viewModels()
    private val adapter = NetworkScanAdapter()
    private var scanning = false
    private val SCAN_DURATION: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment initialized")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Inflating layout")
        binding = FragmentScanNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up UI elements")

        binding.devicesInTheNetwork.layoutManager = LinearLayoutManager(requireContext())
        binding.devicesInTheNetwork.adapter = adapter

        binding.scanScanDevicesButton.speed = 0.1f
        binding.scanScanDevicesButton.playAnimation()

        binding.scanStartScanText.setOnClickListener {
            Log.d(TAG, "scanStartScanText clicked")
            startNetworkScan()
        }

        binding.scanScanDevicesButton.setOnClickListener {
            Log.d(TAG, "scanScanDevicesButton clicked")
            startNetworkScan()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deviceList.collectLatest { devices ->
                    Log.d(TAG, "deviceList observed emission: Found ${devices.size} devices")
                    adapter.submitList(devices)
                }
            }
        }
    }

    private fun startNetworkScan() {
        if (scanning) {
            Log.d(TAG, "startNetworkScan: Already scanning, ignoring tap.")
            return
        }
        Log.i(TAG, "startNetworkScan: Initiating 10-second scan process.")
        scanning = true

        val animation = binding.scanScanDevicesButton
        animation.speed = 1f
        if (!animation.isAnimating) {
            animation.playAnimation()
        }

        val progressBar = binding.scanBarToMove
        progressBar.visibility = View.VISIBLE
        progressBar.translationX = 0f

        val targetWidth = binding.scanOriginalProgressBar.width.toFloat()
        Log.d(TAG, "startNetworkScan: Original Progress Bar Width = $targetWidth")

        progressBar.animate()
            .setDuration(SCAN_DURATION)
            .translationX(if (targetWidth > 0) targetWidth else 1000f) // Fallback translation if layout hasn't measured width
            .withEndAction {
                Log.i(TAG, "startNetworkScan: 10-second scan finished, stopping.")
                animation.speed = 0.1f
                progressBar.visibility = View.INVISIBLE
                viewModel.onEvent(NetworkEvent.StopScan)
                scanning = false
            }
            .start()

        viewModel.onEvent(NetworkEvent.PerformScan)
    }

}