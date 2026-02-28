package com.example.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapters.ChatMessageAdapter
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.databinding.FragmentChatPageBinding
import com.example.chatapp.viewmodels.ChatPageViewModel
import com.google.firebase.auth.FirebaseAuth
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
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        chatPageViewModel.theUserToChat.observe(viewLifecycleOwner) { user ->
            chatPageViewModel.startChatListener(user)
            binding.chattingUserNick.text = user.nick
            Glide.with(requireContext()).load(user.profilePicture).error(R.drawable.outline_photo_camera_24).into(binding.chatProfilePicture)
            Log.d("profile_pic", user.profilePicture!!)
        }

        binding.sendMessageButton.setOnClickListener {
            Log.d("chatMesaj", binding.sendMessageTextField.text.toString())
            chatPageViewModel.addMessageToChat(binding.sendMessageTextField.text.toString())
        }

        val adapter = ChatMessageAdapter(uid, binding.chatMessageRV, chatPageViewModel)
        chatPageViewModel.chat.observe(viewLifecycleOwner) {
            Log.d("listeee", it.toString())
            adapter.submitList(it)
            binding.chatMessageRV.post {
                try {
                    binding.chatMessageRV.smoothScrollToPosition(it.size - 1)
                }
                catch (e: Exception){
                    print(e)
                }

            }
        }

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.chatMessageRV.adapter = adapter
        binding.chatMessageRV.layoutManager = layoutManager

        binding.chatMessageRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if(firstVisible == RecyclerView.NO_POSITION) return
                var i = 0
                for (index in firstVisible..lastVisible){
                    val message = adapter.currentList.get(index) ?: continue
                    if(message.seen == false && message.uid != uid){
                        chatPageViewModel.updateSeenStatus(message, increment = {i++})
                    }
                }

            }
        })

        return binding.root

    }

    override fun onDestroy() {
        chatPageViewModel.clearMessages()
        super.onDestroy()
    }

}