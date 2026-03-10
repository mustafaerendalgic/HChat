package com.example.chatapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.entity.ChatMessage
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.util.hasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class MainPageViewModel @Inject constructor() : ViewModel() {

    private val _userList = MutableLiveData<List<UserListItem>>()
    val userList: LiveData<List<UserListItem>> = _userList

    private val _uid = FirebaseAuth.getInstance().currentUser?.uid

    val userListRef = FirebaseFirestore.getInstance().collection("users")

    private var db = FirebaseDatabase.getInstance()
    private var databaseReference : DatabaseReference? = null
    private var valueEventListener : ValueEventListener? = null

    private lateinit var listListener : ListenerRegistration

    init {

    }

    fun allUsersFlow(): Flow<List<UserListItem>> = callbackFlow {

        val listener = userListRef.addSnapshotListener{snapshot, e ->
            if(e != null){
                close(e)
                Log.d("lastMessageUserFlow", e.toString())
                return@addSnapshotListener
            }

            val userListFlow = snapshot?.documents?.map { doc ->
                UserListItem(
                    nick = doc.getString("nickname") ?: "",
                    uid = doc.id,
                    profilePicture = doc.getString("profile_pic") ?: ""
                )
            } ?: emptyList()

            trySend(userListFlow)
        }

        awaitClose { listener.remove() }

    }

    fun lastMessageFlow(): Flow<Map<String, UserListItem>> = callbackFlow {
        if(_uid == null)
            return@callbackFlow
        val listener = userListRef.document(_uid).collection("recent_chats").addSnapshotListener { snapshot, e ->
            val changes = snapshot?.documents?.associate { doc ->
                doc.id to UserListItem(uid = doc.id, lastMessage = doc.get("lastMessage").toString(), lastMessageBy = doc.get("lastMessageBy").toString(), howManyUnseenMessage = doc.getLong("lastMessageCount")?.toInt() ?: 0)
            } ?: emptyMap()
            trySend(changes)
        }
        awaitClose{listener.remove()}
    }

    val finalUserList : LiveData<List<UserListItem>> = allUsersFlow().combine(lastMessageFlow())
    { allUsers, recentChat ->
        allUsers.map { user ->
            val recentInfo = recentChat[user.uid]
            if(recentInfo != null){
                user.copy(lastMessage = recentInfo.lastMessage, lastMessageBy = recentInfo.lastMessageBy, howManyUnseenMessage = recentInfo.howManyUnseenMessage)
            }
            else{
                user
            }
        }.sortedBy { it.howManyUnseenMessage }
    }.asLiveData()

    fun fetchTheLastMessage(obj: UserListItem){
        if(_uid == null)
            return

        FirebaseFirestore.getInstance().collection("users").document(_uid).collection("recent_chats").document(obj.uid).get().addOnSuccessListener { data ->
            val message = data.get("lastMessage") ?: "Say hi to ${obj.nick}!"
            val byWho = data.get("lastMessageBy") ?: "undefined"
            obj.lastMessage = message.toString()
            obj.lastMessageBy = byWho.toString()
        }

    }

    /*fun fetchUserList(){
        if(_uid == null)
            return

        listListener = userListRef.addSnapshotListener { snapshot, e ->

            if(e != null) {
                Log.d("snapshotListener", e.toString())
                return@addSnapshotListener
            }

            if(snapshot != null){
                Log.d("snapshotListenerFirstLine", snapshot.toString())
                val currentList = _userList.value?.toMutableList() ?: mutableListOf()
                for (dc in snapshot.documentChanges){
                    val userDoc = dc.document

                    val userItem = UserListItem(
                        userDoc.get("nickname") as String,
                        userDoc.id,
                        userDoc.get("profile_pic") as String)
                    Log.d("snapshotListenerUserItem", userItem.toString())
                    when(dc.type){
                        DocumentChange.Type.ADDED -> {
                            if(currentList.none{it.uid == userItem.uid}){
                                currentList.add(userItem)
                                fetchTheLastMessage(userItem)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            currentList.removeAll { it.uid == userItem.uid }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = currentList.indexOfFirst { it.uid == userItem.uid }
                            if(index != -1){
                                currentList[index] = userItem
                                fetchTheLastMessage(userItem)
                            }
                        }
                    }
                }
                _userList.value = currentList
            }
            else{
                Log.d("snapshotListener", "snapshot null")
            }
        }

    }*/

    override fun onCleared() {
        super.onCleared()
        if (::listListener.isInitialized) {
            listListener.remove()
        }
    }

    fun stopListener() {
        if(valueEventListener != null && databaseReference != null){
            databaseReference!!.removeEventListener(valueEventListener!!)
            valueEventListener = null
            databaseReference = null
        }
    }

}