package com.example.chatapp.adapters

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.example.chatapp.R
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.util.hasher
import com.example.chatapp.viewmodels.ChatPageViewModel
import com.google.api.Context
import com.google.firebase.firestore.FirebaseFirestore

class UserListViewHolder(item: View) : RecyclerView.ViewHolder(item){
    val profilePicture: ImageView
    val nickname: TextView
    val lastMessage: TextView
    val messageStatus: ImageView
    init {
        profilePicture = item.findViewById<ImageView>(R.id.profilePhoto)
        nickname = item.findViewById<TextView>(R.id.username)
        lastMessage = item.findViewById<TextView>(R.id.lastMessage)
        messageStatus = item.findViewById<ImageView>(R.id.messageSeen)
    }

}

class UserListAdapter(private val chatPageViewModel: ChatPageViewModel) : ListAdapter<UserListItem, UserListViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserListViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.main_mage_user_list_design, parent, false)
        return UserListViewHolder(v)
    }

    override fun onBindViewHolder(
        holder: UserListViewHolder,
        position: Int
    ) {

        val item = getItem(position)


        Glide.with(holder.itemView.context).load(item.profilePicture).error(R.drawable.outline_photo_camera_24).into(holder.profilePicture)

        holder.lastMessage.text = "Say hi to ${item.nick}!"

        holder.nickname.text = item.nick

        holder.itemView.setOnClickListener {
            chatPageViewModel.setTheUserToChat(item)
            Navigation.findNavController(it).navigate(R.id.mainToChat)
        }

    }
}

class DiffCallback() : ItemCallback<UserListItem>() {
    override fun areItemsTheSame(
        oldItem: UserListItem,
        newItem: UserListItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: UserListItem,
        newItem: UserListItem
    ): Boolean {
        return oldItem.nick == oldItem.nick
    }

}