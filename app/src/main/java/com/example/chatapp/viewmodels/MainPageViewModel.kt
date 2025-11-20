package com.example.chatapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@HiltViewModel
class MainPageViewModel @Inject constructor(val repo: Repo) : ViewModel() {

    private val _temp = MutableLiveData<String>()
    val temp: LiveData<String> = _temp

    private val _latitude = MutableLiveData<Float>()
    val latitude: LiveData<Float> = _latitude
    private val _longitude = MutableLiveData<Float>()
    val longitude: LiveData<Float> = _longitude

    private val _userList = MutableLiveData<List<String>>()
    val userList: LiveData<List<String>> = _userList

    val userListRef = FirebaseFirestore.getInstance().collection("users")

    fun assignTemp(){
        viewModelScope.launch {
        try {
            _temp.value = repo.getTheWeatherData(
                latitude.value!!.toFloat(),
                longitude.value!!.toFloat()
            ).current.temperature_2m.toString()
            Log.d("sicaklik", _temp.value.toString())

        }
            catch (e: Exception){
                Log.d("sicaklik", e.toString())
            }

        }
    }

    fun updateLatLong(latitude: Float, longitude: Float){
        _latitude.value = latitude
        _longitude.value = longitude
    }

    fun fetchUserList(){

        userListRef.document("userMap").get().addOnSuccessListener { document ->
            val userMap = document.data as? Map<String, String>
            if(document.data.isNullOrEmpty()){
                _userList.value = emptyList()
            }
            else{
                _userList.value = userMap!!.values.toList()
            }

        }

    }

}