package com.example.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapters.UserListAdapter
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.databinding.FragmentMainPageBinding
import com.example.chatapp.viewmodels.ChatPageViewModel
import com.example.chatapp.viewmodels.MainPageViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainPage : Fragment() {


    val chatPageViewModel: ChatPageViewModel by activityViewModels()

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

        val userListAdapter = UserListAdapter(chatPageViewModel)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        mainPageViewModel.userList.observe(viewLifecycleOwner) { data ->
            userListAdapter.submitList(data)
        }
        binding.userList.adapter = userListAdapter
        binding.userList.layoutManager = layoutManager

        binding.button.setOnClickListener {
            Log.d("buton_basildi", "Buton Basıldı")
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.mainToLogin)
        }

        return binding.root
    }
}