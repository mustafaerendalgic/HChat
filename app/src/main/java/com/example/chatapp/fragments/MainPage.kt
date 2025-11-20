package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapters.UserListAdapter
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.databinding.FragmentMainPageBinding
import com.example.chatapp.viewmodels.MainPageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainPage : Fragment() {

    val viewmodel: MainPageViewModel by activityViewModels()

    private lateinit var binding: FragmentMainPageBinding
    private val mainPageViewModel: MainPageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPageBinding.inflate(inflater, container, false)

        mainPageViewModel.fetchUserList()

        val userListAdapter = UserListAdapter()
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        var userList = listOf<UserListItem>()

        mainPageViewModel.userList.observe(viewLifecycleOwner) { data ->
            userListAdapter.submitList(data)
        }

        userListAdapter.submitList(userList)
        binding.userList.adapter = userListAdapter
        binding.userList.layoutManager = layoutManager

        return binding.root
    }
}