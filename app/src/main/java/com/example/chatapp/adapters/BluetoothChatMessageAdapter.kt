package com.example.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.data.entity.BluetoothMessage
import com.example.chatapp.data.entity.ChatMessage
import com.example.chatapp.util.formatDate
import com.example.chatapp.util.formatDateBluetooth
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore


class BSenderMessageViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val senderTimestamp: TextView = item.findViewById(R.id.timestamp)
    val senderMessage: TextView = item.findViewById(R.id.senderMessage)
}

class BReceiverMessageViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val receiverProfilePicture: ShapeableImageView = item.findViewById(R.id.receiverProfilePicture)
    val receiverNickname: TextView = item.findViewById(R.id.receiverNickname)
    val receiverMessage: TextView = item.findViewById(R.id.messageReceiver)
    val timestamp: TextView = item.findViewById(R.id.timestampReceiver)
}

class BluetoothChatMessageAdapter(): ListAdapter<BluetoothMessage, RecyclerView.ViewHolder>(DiffCallbackForBluetoothChat()){

    val RECEIVER_VIEW_TYPE = 0
    val SENDER_VIEW_TYPE = 1

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item.isSentByMe){
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
                BReceiverMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_sender_layout, parent, false)
                BSenderMessageViewHolder(view)
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
                holder as BReceiverMessageViewHolder

                holder.receiverNickname.text = message.nickname
                holder.timestamp.text = formatDateBluetooth(message.timestamp)
                holder.receiverMessage.text = message.message

            }
            else -> {
                holder as BSenderMessageViewHolder
                Log.d("mesupdate", "created as not seen")

                holder.senderMessage.text = message.message
                holder.senderTimestamp.text = formatDateBluetooth(message.timestamp)
            }
        }
    }

}

class DiffCallbackForBluetoothChat: DiffUtil.ItemCallback<BluetoothMessage>(){

    override fun areItemsTheSame(
        oldItem: BluetoothMessage,
        newItem: BluetoothMessage
    ): Boolean {
        return oldItem.timestamp == newItem.timestamp && oldItem.message == newItem.message
    }

    override fun areContentsTheSame(
        oldItem: BluetoothMessage,
        newItem: BluetoothMessage
    ): Boolean {
        return oldItem == newItem
    }

}