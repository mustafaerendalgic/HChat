package com.example.chatapp.internet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentMainMenuBinding

class MainMenu : Fragment() {

    private lateinit var binding: FragmentMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainMenuBinding.inflate(inflater)

        binding.internetModuleNavigationButton.setOnClickListener {
            findNavController().navigate(R.id.main_to_i)
        }

        binding.bluetoothModuleNavigationButton.setOnClickListener {
            findNavController().navigate(R.id.main_to_b)
        }

        binding.networkModuleNavigationButton.setOnClickListener {
            findNavController().navigate(R.id.main_to_b)
        }

        return binding.root
    }

}