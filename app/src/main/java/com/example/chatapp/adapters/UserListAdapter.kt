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
import com.example.chatapp.R
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.viewmodels.ChatPageViewModel
import com.google.firebase.auth.FirebaseAuth

class seenUserListViewHolder(item: View) : RecyclerView.ViewHolder(item){
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

class unseenUserListViewHolder(item: View) : RecyclerView.ViewHolder(item){
    val profilePicture: ImageView
    val nickname: TextView
    val lastMessage: TextView
    val lastMessageDate: TextView
    val howManyText: TextView
    val howManyBalloon: ImageView
    init {
        profilePicture = item.findViewById<ImageView>(R.id.unseen_profilePhoto)
        nickname = item.findViewById<TextView>(R.id.unseen_username)
        lastMessage = item.findViewById<TextView>(R.id.unseen_lastMessage)
        lastMessageDate = item.findViewById<TextView>(R.id.unseen_lastMessageDate)
        howManyText = item.findViewById<TextView>(R.id.howManyUnseenText)
        howManyBalloon = item.findViewById<ImageView>(R.id.howManyUnseenBalloon)
    }

}

class UserListAdapter(private val chatPageViewModel: ChatPageViewModel) : ListAdapter<UserListItem, RecyclerView.ViewHolder>(DiffCallback()) {
    private val UNSEEN = 0
    private val SEEN = 1
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.lastMessageStatus == 1)
            UNSEEN
        else
            SEEN
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            SEEN -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.main_mage_user_list_design, parent, false)
                seenUserListViewHolder(v)
            }
            UNSEEN -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.main_mage_new_message_user_list_design, parent, false)
                unseenUserListViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.main_mage_user_list_design, parent, false)
                seenUserListViewHolder(v)
            }
        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        val item = getItem(position)

        when(holder.itemViewType){
            SEEN -> {
                holder as seenUserListViewHolder
                Glide.with(holder.itemView.context).load(item.profilePicture).error(R.drawable.outline_photo_camera_24).into(holder.profilePicture)

                if(item.lastMessage == null){
                    holder.lastMessage.text =  "Say hi to ${item.nick}!"
                }
                else{
                    holder.messageStatus.visibility = View.VISIBLE
                    if(item.lastMessageBy == uid){
                        holder.lastMessage.text = "You: " + item.lastMessage
                    }
                    else{
                        holder.lastMessage.text = item.lastMessage
                    }
                }

                holder.nickname.text = if(uid != item.uid)item.nick else item.nick + " (You)"

                holder.itemView.setOnClickListener {
                    chatPageViewModel.setTheUserToChat(item)
                    Navigation.findNavController(it).navigate(R.id.mainToChat)
                }
            }
            UNSEEN -> {
                holder as unseenUserListViewHolder

                Glide.with(holder.itemView.context).load(item.profilePicture).error(R.drawable.outline_photo_camera_24).into(holder.profilePicture)

                holder.howManyText.text = item.howManyUnseenMessage.toString()

                holder.lastMessage.text = item.lastMessage

                holder.lastMessageDate.text = item.lastMessageDate

                holder.nickname.text = if(uid != item.uid)item.nick else item.nick + " (You)"

                holder.itemView.setOnClickListener {
                    chatPageViewModel.setTheUserToChat(item)
                    Navigation.findNavController(it).navigate(R.id.mainToChat)
                }

            }
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