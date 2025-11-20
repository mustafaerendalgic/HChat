package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentMainPageBinding
import com.example.chatapp.viewmodels.MainPageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainPage : Fragment() {

    val viewmodel: MainPageViewModel by activityViewModels()

    private lateinit var binding: FragmentMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPageBinding.inflate(inflater, container, false)

        var latitude = 52.52f
        var longitude = 13.41f

        viewmodel.updateLatLong(latitude, longitude)

        viewmodel.assignTemp()

        binding.userList

        return binding.root
    }
}