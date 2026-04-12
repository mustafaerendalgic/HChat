package com.example.chatapp.viewmodels.InternetModule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.entity.ChatMessage
import com.example.chatapp.data.entity.UserListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class InternetChatPageViewModel @Inject constructor(): ViewModel() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val _theUserToChat = MutableLiveData<UserListItem>()
    val theUserToChat : LiveData<UserListItem> = _theUserToChat

    private val _chat = MutableLiveData< ArrayList<ChatMessage> >()
    val chat : LiveData<ArrayList<ChatMessage>> = _chat

    private var valueEventListener : ValueEventListener? = null
    private var databaseRef : DatabaseReference? = null
    private val database = FirebaseDatabase.getInstance()

    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm")
    val currentDate = sdf.format(Date())

    fun addMessageToChat(message: String){
        if(uid == null)
            return

        val partnerUid = _theUserToChat.value!!.uid
        val userRef = FirebaseFirestore.getInstance().collection("users")

        val recentRef = userRef.document(uid).collection("recent_chats").document(partnerUid)
        val recentRefPartner = userRef.document(partnerUid).collection("recent_chats").document(uid)

        val fileName = minOf(uid, partnerUid).toString() + "-" + maxOf(uid, partnerUid).toString()

        databaseRef = database.getReference("chats").child(fileName)
        userRef.document(uid).get().addOnSuccessListener { data ->
            val messRef = databaseRef!!.push()
            val messageObject = ChatMessage(messRef.key , uid, message, currentDate, "0", data.get("nickname").toString(), false)
            Log.d("mesupdate", messageObject.messageID.toString() + " obj key")
            messRef.setValue(messageObject)

            val batch = FirebaseFirestore.getInstance().batch()

            val updateYourLastMessage = mapOf("lastMessage" to message, "lastMessageBy" to uid, "timestamp" to FieldValue.serverTimestamp())
            val updatePartnerLastMessage = mapOf("lastMessage" to message, "lastMessageBy" to uid, "timestamp" to FieldValue.serverTimestamp())

            batch.set(recentRefPartner, updatePartnerLastMessage, SetOptions.merge())
            batch.set(recentRef, updateYourLastMessage, SetOptions.merge())

            batch.commit().addOnSuccessListener {
                recentRefPartner.get().addOnSuccessListener { recentSnapshot ->
                    val lastCount = recentSnapshot.getLong("unseenMessageCount")?.toInt() ?: 0
                    recentRefPartner.set(mapOf("unseenMessageCount" to lastCount + 1), SetOptions.merge())
                }
            }    .addOnFailureListener {
                Log.d("recentChat", it.toString())
            }

        }
            .addOnFailureListener {
                Log.d("mesupdateFailure", it.toString())
            }
    }

    fun startChatListener(partner: UserListItem){

        if(uid == null)
            return

        stopChatListener()

        val partnerUid = _theUserToChat.value!!.uid
        val fileName = minOf(uid, partnerUid).toString() + "-" + maxOf(uid, partnerUid).toString()

        val dbRef = database.getReference("chats").child(fileName)
        valueEventListener = dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedChat = ArrayList<ChatMessage>()
                for (messageSnapshot in snapshot.children){

                    val chatMessage = messageSnapshot.getValue(ChatMessage::class.java)

                    if(chatMessage != null){
                        updatedChat.add(chatMessage)
                    }

                }
                _chat.value = updatedChat
                Log.d("fatal", updatedChat.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                print(error)
            }

        })
    }

    fun updateSeenStatus(obj : ChatMessage){
        if(uid == null)
            return
        val partnerUid = _theUserToChat.value!!.uid
        Log.d("mesupdate1", obj.toString())

        val fileName = minOf(uid, partnerUid).toString() + "-" + maxOf(uid, partnerUid).toString()
        val dbRef = FirebaseDatabase.getInstance().reference.child("chats").child(fileName).child(obj.messageID.toString())

        dbRef.updateChildren(mapOf("seen" to true)).addOnSuccessListener {
            Log.d("mesupdate1", "Başarılı")

        }.addOnFailureListener {
            Log.d("mesupdate2", "Başarısız" + it.toString())
        }

    }

    fun updateSeenCount(i: Int){
        if(uid == null) {
            Log.d("updateSeen", "uid null")
            return
        }

        if(_theUserToChat.value == null){
            Log.d("updateSeen", "partner uid null")
            return
        }

        val partnerUid = _theUserToChat.value!!.uid

        val userRef = FirebaseFirestore.getInstance().collection("users")
        val recentRef = userRef.document(uid).collection("recent_chats").document(partnerUid)

        recentRef.update("unseenMessageCount", FieldValue.increment(-i.toLong())).addOnSuccessListener {
            recentRef.get().addOnSuccessListener { snapshot ->
                val current = snapshot.getLong("unseenMessageCount") ?: 0
                if (current < 0) {
                    recentRef.update("unseenMessageCount", 0)
                }
            }
        }

    }

    fun setTheUserToChat(user: UserListItem){
        _theUserToChat.value = user
    }

    fun stopChatListener(){
        if(valueEventListener != null && databaseRef != null) {
            Log.d("fatal", "chat listener is stopped")
            clearMessages()
            databaseRef!!.removeEventListener(valueEventListener!!)
            databaseRef = null
            valueEventListener = null
        }
    }

    fun clearMessages(){
        val list = ArrayList<ChatMessage>()
        _chat.value = list
    }

}