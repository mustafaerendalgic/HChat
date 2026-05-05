package com.example.chatapp.internet.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.adapters.UserListAdapter
import com.example.chatapp.databinding.IFragmentMainPageBinding
import com.example.chatapp.internet.viewmodels.InternetChatPageViewModel
import com.example.chatapp.internet.viewmodels.InternetMainPageViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InternetMainPage : Fragment() {


    val internetChatPageViewModel: InternetChatPageViewModel by activityViewModels()

    private lateinit var binding: IFragmentMainPageBinding
    private val internetMainPageViewModel: InternetMainPageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = IFragmentMainPageBinding.inflate(inflater, container, false)

        val userListAdapter = UserListAdapter(internetChatPageViewModel)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        internetMainPageViewModel.finalUserList.observe(viewLifecycleOwner) { list ->
            userListAdapter.submitList(list)
        }
        binding.iUserList.adapter = userListAdapter
        binding.iUserList.layoutManager = layoutManager

        binding.button.setOnClickListener {
            Log.d("buton_basildi", "Buton Basıldı")
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.mainToLogin)
        }

        return binding.root
    }
}