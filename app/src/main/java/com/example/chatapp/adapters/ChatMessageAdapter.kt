package com.example.chatapp.adapters

import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.data.entity.ChatMessage
import com.google.android.material.imageview.ShapeableImageView
import org.w3c.dom.Text

class SenderMessageViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val senderTimestamp: TextView = item.findViewById(R.id.timestamp)
    val senderMessage: TextView = item.findViewById(R.id.senderMessage)
    val senderDelivered: ImageView = item.findViewById(R.id.senderDelivered)
}

class ReceiverMessageViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val receiverProfilePicture: ShapeableImageView = item.findViewById(R.id.receiverProfilePicture)
    val receiverNickname: TextView = item.findViewById(R.id.receiverNickname)
    val receiverMessage: TextView = item.findViewById(R.id.messageReceiver)
    val timestamp: TextView = item.findViewById(R.id.timestampReceiver)
}

class ChatMessageAdapter(private val uid: String): ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallbackForChat()){

    val RECEIVER_VIEW_TYPE = 0
    val SENDER_VIEW_TYPE = 1

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item.uid == uid){
            SENDER_VIEW_TYPE
        }
        else{
            RECEIVER_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            RECEIVER_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_receiver_layout, parent, false)
                ReceiverMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_sender_layout, parent, false)
                SenderMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = getItem(position)
        when(holder.itemViewType){
            RECEIVER_VIEW_TYPE -> {
                holder as ReceiverMessageViewHolder
                holder.receiverNickname.text = message.message
                holder.timestamp.text = message.timestamp
                holder.receiverMessage.text = message.message
                Glide.with(holder.itemView).load(message.profilePicture).error(R.drawable.outline_photo_camera_24).into(holder.receiverProfilePicture)
            }
            else -> {
                holder as SenderMessageViewHolder
                holder.senderMessage.text = message.message
                holder.senderTimestamp.text = message.timestamp
            }
        }
    }

}

class DiffCallbackForChat: DiffUtil.ItemCallback<ChatMessage>(){
    override fun areItemsTheSame(
        oldItem: ChatMessage,
        newItem: ChatMessage
    ): Boolean {
        return oldItem.messageID == newItem.messageID
    }

    override fun areContentsTheSame(
        oldItem: ChatMessage,
        newItem: ChatMessage
    ): Boolean {
        return oldItem == newItem
    }

}