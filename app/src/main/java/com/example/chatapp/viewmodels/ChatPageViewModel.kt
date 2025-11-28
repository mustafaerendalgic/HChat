package com.example.chatapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.entity.UserListItem
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatPageViewModel @Inject constructor(): ViewModel() {

    private val _theUserToChat = MutableLiveData<UserListItem>()
    val theUserToChat : LiveData<UserListItem> = _theUserToChat

    fun setTheUserToChat(user: UserListItem){
        _theUserToChat.value = user
    }

}