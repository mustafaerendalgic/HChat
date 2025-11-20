package com.example.chatapp.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.util.hasher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val uid = FirebaseAuth.getInstance().uid.toString()
    private val usersRef = FirebaseFirestore.getInstance().collection("users")
    private val emailRef = FirebaseFirestore.getInstance().collection("emails")

    private fun signUpUser(email: String, password: String, nickname: String, context: Context){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(context, "Başarılı", Toast.LENGTH_SHORT).show()
                usersRef.document(nickname).set(mapOf(hasher(uid) to email),
                    SetOptions.merge())
                emailRef.document(email).set(mapOf(hasher(uid) to email))
            }.addOnFailureListener {
                Toast.makeText(context, "Başarısız ${it}", Toast.LENGTH_SHORT).show()
            }
    }

    fun checkAndSignUserUp(email: String, password: String, nickname: String, context: Context): Boolean{

        var check = false

        if(nickname.contains(" ")){
            Toast.makeText(context, ContextCompat.getString(context, R.string.nicknamesBlank),
                Toast.LENGTH_SHORT).show()
        }
        else {

            usersRef.document(nickname).get().addOnSuccessListener { data ->
                if(data.exists()){
                    Toast.makeText(context, ContextCompat.getString(context, R.string.nickNameIsUsed), Toast.LENGTH_SHORT).show()
                }
                else{
                    emailRef.document(email).get().addOnSuccessListener {
                        if(it.exists()){
                            Toast.makeText(context, ContextCompat.getString(context, R.string.emailIsUsed), Toast.LENGTH_SHORT).show()
                        }
                        else{
                            signUpUser(email, password, nickname, context)
                        }
                    }
                }
            }


        }

        return check

    }

}