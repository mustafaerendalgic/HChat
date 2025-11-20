package com.example.chatapp.adapters

import android.media.Image
import android.view.LayoutInflater
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

class UserListAdapter() : ListAdapter<UserListItem, UserListViewHolder>(DiffCallback()) {
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

        holder.nickname.text = item.nick

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