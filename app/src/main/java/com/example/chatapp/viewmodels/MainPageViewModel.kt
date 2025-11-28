package com.example.chatapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.entity.UserListItem
import com.example.chatapp.util.hasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

@HiltViewModel
class MainPageViewModel @Inject constructor() : ViewModel() {

    private val _userList = MutableLiveData<List<UserListItem>>()
    val userList: LiveData<List<UserListItem>> = _userList

    val userListRef = FirebaseFirestore.getInstance().collection("users")

    init {
        fetchUserList()
    }

    fun fetchUserList(){
        val list = ArrayList<UserListItem>()
        userListRef.get().addOnSuccessListener { data ->

            for (document in data.documents){
                list.add(UserListItem(profilePicture = document.get("profile_pic") as String, nick = document.id.toString()))
            }
            _userList.value = list
            Log.d("userlist", "${list}")
        }

    }

}