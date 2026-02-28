package com.example.chatapp.adapters

import android.media.browse.MediaBrowser
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.data.entity.ChatMessage
import com.example.chatapp.util.formatDate
import com.example.chatapp.viewmodels.ChatPageViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.api.Distribution
import com.google.firebase.firestore.FirebaseFirestore
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

class ChatMessageAdapter(private val uid: String, private val rv: RecyclerView, private val chatPageViewModel: ChatPageViewModel): ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallbackForChat()){

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

                holder.receiverNickname.text = message.nickname
                holder.timestamp.text = formatDate(message.timestamp)
                holder.receiverMessage.text = message.message
                FirebaseFirestore.getInstance().collection("users").document(message.uid!!).get().addOnSuccessListener {
                    Glide.with(holder.itemView).load(it.get("profile_pic") as String).error(R.drawable.outline_photo_camera_24).into(holder.receiverProfilePicture)
                }.addOnFailureListener {
                    Toast.makeText(holder.itemView.context, it.toString(), Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                holder as SenderMessageViewHolder
                if(message.seen == true){
                    holder.senderDelivered.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.outline_arrows_more_up_24))
                }
                else {
                    holder.senderDelivered.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.itemView.context,
                            R.drawable.arrow_up
                        )
                    )
                    Log.d("mesupdate", "created as not seen")
                }
                holder.senderMessage.text = message.message
                holder.senderTimestamp.text = formatDate(message.timestamp)
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
        return oldItem.message == newItem.message &&
                oldItem.seen == newItem.seen &&
                oldItem.timestamp == newItem.timestamp
    }

}