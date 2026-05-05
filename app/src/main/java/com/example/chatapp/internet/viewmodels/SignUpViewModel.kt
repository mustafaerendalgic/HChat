package com.example.chatapp.internet.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val uid = FirebaseAuth.getInstance().uid.toString()
    private val usersRef = FirebaseFirestore.getInstance().collection("users")
    private val emailRef = FirebaseFirestore.getInstance().collection("emails")
    private val storage = FirebaseStorage.getInstance().reference


    private fun signUpUser(email: String, password: String, nickname: String, context: Context, button: AppCompatButton, profilePicture: Uri, onResult: () -> Unit){

        button.isEnabled = false
        button.alpha = 0.5f

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { user ->

                Toast.makeText(context, "Başarılı", Toast.LENGTH_SHORT).show()

                emailRef.document(email).set(mapOf("nickname" to nickname))
                val ppRef = storage.child(nickname).child("profile-picture.jpg")

                ppRef.putFile(profilePicture).addOnSuccessListener {

                    ppRef.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        usersRef.document(user.user!!.uid).set(mapOf("profile_pic" to imageUrl, "nickname" to nickname))
                        Log.d("profilepic", imageUrl)
                    }

                }.addOnFailureListener {
                    Log.d("profilepic", it.toString())
                }

                onResult()

                button.isEnabled = true
                button.alpha = 1f
            }.addOnFailureListener {
                Toast.makeText(context, "Başarısız2 ${it}", Toast.LENGTH_SHORT).show()
                button.isEnabled = true
                button.alpha = 1f
            }
    }

    fun checkAndSignUserUp(email: String, password: String, nickname: String, context: Context, button: AppCompatButton, profilePicture: Uri, onResult : () -> Unit): Boolean{

        button.isEnabled = false
        button.alpha = 0.5f

        var check = false

        if(nickname.contains(" ")){
            Toast.makeText(context, ContextCompat.getString(context, R.string.nicknamesBlank), Toast.LENGTH_SHORT).show()
            button.isEnabled = true
            button.alpha = 1f
        }
        else {

            usersRef.document(nickname).get().addOnSuccessListener { data ->
                if(data.exists()){
                    button.isEnabled = true
                    button.alpha = 1f
                    Toast.makeText(context, ContextCompat.getString(context, R.string.nickNameIsUsed), Toast.LENGTH_SHORT).show()
                }
                else{
                    emailRef.document(nickname).get().addOnSuccessListener {
                        if(it.exists()){
                            button.isEnabled = true
                            button.alpha = 1f
                            Toast.makeText(context, ContextCompat.getString(context, R.string.emailIsUsed), Toast.LENGTH_SHORT).show()
                        }
                        else{
                            signUpUser(email, password, nickname, context, button, profilePicture, onResult)
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                button.isEnabled = true
                button.alpha = 1f
            }

        }

        return check

    }

}