package com.example.chatapp.adapters

import android.bluetooth.BluetoothClass
import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.data.entity.BluetoothDeviceListItem

class bltSeenUserListViewHolder(item: View) : RecyclerView.ViewHolder(item){
    val profilePicture: ImageView
    val nickname: TextView
    val lastMessage: TextView
    val lastMessageDate: TextView
    val deviceIcon: ImageView
    val chatIcon: ImageView
    init {
        profilePicture = item.findViewById<ImageView>(R.id.b_profile_photo)
        nickname = item.findViewById<TextView>(R.id.b_username)
        lastMessage = item.findViewById<TextView>(R.id.b_last_message)
        lastMessageDate = item.findViewById<TextView>(R.id.b_last_message_date)
        deviceIcon = item.findViewById<ImageView>(R.id.b_device_icon)
        chatIcon = item.findViewById<ImageView>(R.id.bluetooth_chat_icon)
    }
}

class bltUnseenUserListViewHolder(item: View) : RecyclerView.ViewHolder(item){
    val profilePicture: ImageView
    val nickname: TextView
    val lastMessage: TextView
    val lastMessageDate: TextView
    val howManyText: TextView
    val howManyBalloon: ImageView
    val deviceIcon: ImageView
    init {
        profilePicture = item.findViewById<ImageView>(R.id.b_unseen_profile_photo)
        nickname = item.findViewById<TextView>(R.id.b_unseen_username)
        lastMessage = item.findViewById<TextView>(R.id.b_unseen_lastMessage)
        lastMessageDate = item.findViewById<TextView>(R.id.b_unseen_lastMessageDate)
        howManyText = item.findViewById<TextView>(R.id.b_how_many_unseen_text)
        howManyBalloon = item.findViewById<ImageView>(R.id.b_how_many_unseen_balloon)
        deviceIcon = item.findViewById<ImageView>(R.id.b_unseen_device_icon)
    }
}

class BluetoothUserListAdapter(private val onDeviceClick: (device: BluetoothDeviceListItem) -> Unit) : ListAdapter<BluetoothDeviceListItem, RecyclerView.ViewHolder>(
    BluetoothDeviceListDiffCallback()
) {
    val DELIVER_ERROR = 3
    val UNDELIVERED = 2
    val SEEN_ITEM_VIEW = 1
    val UNSEEN_ITEM_VIEW = 0

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.lastMessageStatus == SEEN_ITEM_VIEW)
            SEEN_ITEM_VIEW
        else
            UNSEEN_ITEM_VIEW
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == UNSEEN_ITEM_VIEW){
            val v = LayoutInflater.from(parent.context).inflate(R.layout.b_main_page_new_message_user_list_design, parent, false)
            return bltUnseenUserListViewHolder(v)
        }
        else {
           val v = LayoutInflater.from(parent.context).inflate(R.layout.b_main_page_user_list_design, parent, false)
            return bltSeenUserListViewHolder(v)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        if(getItemViewType(position) == UNSEEN_ITEM_VIEW){
            holder as bltUnseenUserListViewHolder
            holder.lastMessage.text = item.lastMessage
            holder.lastMessageDate.text = item.lastMessageDate
            holder.nickname.text = item.deviceName
            holder.howManyText.text = item.howManyUnseen.toString()
            if(item.isConnected)
                holder.nickname.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
            if(item.profilePicture != null)
                holder.profilePicture.setImageURI(item.profilePicture)
            holder.itemView.setOnClickListener {
                    onDeviceClick(item)
            }
        }
        else{
            holder as bltSeenUserListViewHolder
            if(item.lastMessage.isNullOrEmpty()){
                holder.lastMessage.text = "Tap to establish a connection"
                holder.lastMessageDate.visibility = View.GONE
            }
            else{
                holder.lastMessage.text = item.lastMessage
                holder.lastMessageDate.visibility = View.VISIBLE
                holder.lastMessageDate.text = item.lastMessageDate
            }
            holder.nickname.text = item.deviceName

            if(item.isConnected){
                holder.nickname.setTextColor(holder.itemView.context.getColor(R.color.b_primary))
                holder.chatIcon.visibility = View.VISIBLE
                holder.chatIcon.isEnabled = true
                holder.chatIcon.setOnClickListener { holder.itemView.findNavController().navigate(R.id.b_main_to_chat) }
            }
            else{
                holder.nickname.setTextColor(holder.itemView.context.getColor(R.color.textColor))
                holder.chatIcon.visibility = View.GONE
                holder.chatIcon.isEnabled = false
            }

            if(item.profilePicture != null)
                holder.profilePicture.setImageURI(item.profilePicture)
            if(item.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER){
                holder.deviceIcon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.baseline_computer_24))
            }
            holder.itemView.setOnClickListener {
                onDeviceClick(item)
            }
        }
    }

}

class BluetoothDeviceListDiffCallback() : DiffUtil.ItemCallback<BluetoothDeviceListItem>(){
    override fun areItemsTheSame(
        oldItem: BluetoothDeviceListItem,
        newItem: BluetoothDeviceListItem
    ): Boolean {
        return if (oldItem.macAddress == newItem.macAddress)
            true
        else
            false
    }

    override fun areContentsTheSame(
        oldItem: BluetoothDeviceListItem,
        newItem: BluetoothDeviceListItem
    ): Boolean {
        return if (oldItem.equals(newItem))
            true
        else
            false
    }

}