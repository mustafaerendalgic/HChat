package com.example.chatapp.fragments.InternetModule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapters.ChatMessageAdapter
import com.example.chatapp.databinding.IFragmentChatPageBinding
import com.example.chatapp.viewmodels.InternetModule.InternetChatPageViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InternetChatPage : Fragment() {

    private lateinit var binding: IFragmentChatPageBinding
    val internetChatPageViewModel: InternetChatPageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = IFragmentChatPageBinding.inflate(inflater, container, false)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        internetChatPageViewModel.theUserToChat.observe(viewLifecycleOwner) { user ->
            internetChatPageViewModel.startChatListener(user)
            binding.iChattingUserNick.text = user.nick
            Glide.with(requireContext()).load(user.profilePicture).error(R.drawable.outline_photo_camera_24).into(binding.iChatProfilePicture)
            Log.d("profile_pic", user.profilePicture!!)
        }

        binding.iSendMessageButton.setOnClickListener {
            Log.d("chatMesaj", binding.iSendMessageTextField.text.toString())
            internetChatPageViewModel.addMessageToChat(binding.iSendMessageTextField.text.toString())
        }

        val adapter = ChatMessageAdapter(uid, binding.iChatMessageRV, internetChatPageViewModel)

        internetChatPageViewModel.chat.observe(viewLifecycleOwner) {
            Log.d("listeee", it.toString())
            val lastMessage = it.lastOrNull()
            Log.d("lastMessage", lastMessage.toString())
            if(lastMessage != null && !lastMessage.seen && lastMessage.uid != uid) {
                internetChatPageViewModel.updateSeenStatus(lastMessage)
                internetChatPageViewModel.updateSeenCount(1)
            }
            adapter.submitList(it)
            binding.iChatMessageRV.post {
                try {
                    binding.iChatMessageRV.smoothScrollToPosition(it.size - 1)
                }
                catch (e: Exception){
                    print(e)
                }

            }
        }

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.iChatMessageRV.adapter = adapter
        binding.iChatMessageRV.layoutManager = layoutManager

        binding.iChatMessageRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    if(firstVisible == RecyclerView.NO_POSITION) return
                    var i = 0
                    for(index in firstVisible..lastVisible){
                        val message = adapter.currentList.get(index) ?: continue
                        if(message.seen == false && message.uid != uid){
                            i++
                            internetChatPageViewModel.updateSeenStatus(message)
                        }
                    }
                    if(i > 0)
                        internetChatPageViewModel.updateSeenCount(i)
                }
            }

        })

        return binding.root

    }

    override fun onDestroy() {
        internetChatPageViewModel.clearMessages()
        super.onDestroy()
    }

}