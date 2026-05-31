package com.example.chatapp.scan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.scan.data.entity.ScanResultObject

class NetworkScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val deviceIcon: ImageView = itemView.findViewById(R.id.device_icon_container)
    val tvHostName: TextView = itemView.findViewById(R.id.tv_host_name)
    val tvIpAddress: TextView = itemView.findViewById(R.id.tv_ip_address)
    val tvMacAddress: TextView = itemView.findViewById(R.id.tv_mac_address)
}

class NetworkScanAdapter : ListAdapter<ScanResultObject, NetworkScanViewHolder>(ScanResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkScanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_result_item, parent, false)
        return NetworkScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: NetworkScanViewHolder, position: Int) {
        val item = getItem(position)
        
        val context = holder.itemView.context
        
        // Premium feature: determine icon dynamically based on hostname
        val hostLower = item.hostName.lowercase()
        if (hostLower.contains("pc") || hostLower.contains("desktop") || 
            hostLower.contains("computer") || hostLower.contains("macbook") || 
            hostLower.contains("laptop") || hostLower.contains("workstation")) {
            holder.deviceIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_computer_24))
        } else {
            holder.deviceIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_smartphone_24))
        }
        
        holder.tvHostName.text = if (item.hostName.isNullOrEmpty() || item.hostName == item.ipAddress) {
            "Host: Unknown Device"
        } else {
            "Host: ${item.hostName}"
        }
        
        holder.tvIpAddress.text = "IP: ${item.ipAddress}"
        holder.tvMacAddress.text = "MAC: ${item.macAddress}"
    }
}

class ScanResultDiffCallback : DiffUtil.ItemCallback<ScanResultObject>() {
    override fun areItemsTheSame(oldItem: ScanResultObject, newItem: ScanResultObject): Boolean {
        return oldItem.ipAddress == newItem.ipAddress
    }

    override fun areContentsTheSame(oldItem: ScanResultObject, newItem: ScanResultObject): Boolean {
        return oldItem == newItem
    }
}
