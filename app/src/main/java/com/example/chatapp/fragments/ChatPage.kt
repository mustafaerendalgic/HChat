package com.example.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentChatPageBinding
import com.example.chatapp.viewmodels.ChatPageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatPage : Fragment() {

    private lateinit var binding: FragmentChatPageBinding
    val chatPageViewModel: ChatPageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatPageBinding.inflate(inflater, container, false)
        chatPageViewModel.theUserToChat.observe(viewLifecycleOwner) { user ->
            binding.chattingUserNick.text = user.nick
            Glide.with(requireContext()).load(user.profilePicture).error(R.drawable.outline_photo_camera_24).into(binding.chatProfilePicture)
            Log.d("profile_pic", user.profilePicture)
        }
        return binding.root
    }
}