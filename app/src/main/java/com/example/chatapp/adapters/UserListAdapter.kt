package com.example.chatapp.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.data.entity.UserListItem

class UserListViewHolder(item: View) : RecyclerView.ViewHolder(item){

    init {
        val profilePicture = item.findViewById<ImageView>(R.id.profilePhoto)
        val nickname = item.findViewById<TextView>(R.id.nicknameTextField)
        val lastMessage = item.findViewById<TextView>(R.id.lastMessage)
        val messageStatus = item.findViewById<ImageView>(R.id.messageSeen)
    }

}

class UserListAdapter() : ListAdapter<UserListItem, UserListViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserListViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: UserListViewHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
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