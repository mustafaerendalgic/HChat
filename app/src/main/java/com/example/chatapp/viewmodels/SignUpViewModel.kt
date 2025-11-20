package com.example.chatapp.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.util.hasher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUpViewModel {

    private val uid = FirebaseAuth.getInstance().uid.toString()
    private val usersRef = FirebaseFirestore.getInstance().collection("users")

    private fun signUpUser(email: String, password: String, nickname: String, context: Context){

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(context, "Başarılı", Toast.LENGTH_SHORT).show()
                usersRef.document("userMap").set(mapOf(hasher(uid) to nickname),
                    SetOptions.merge())
                usersRef.document("userNicknameMap").set(mapOf("nicks" to nickname), SetOptions.merge())
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

            usersRef.document("userNicknameMap").get().addOnSuccessListener { data ->

                if(data.exists()) {
                    val map = data.data as? Map<String, String>
                    if (map!!.values.contains(nickname)) {
                        Toast.makeText(
                            context,
                            "${ContextCompat.getString(context, R.string.nickNameIsUsed)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        signUpUser(email, password, nickname, context)
                    }
                }
                else{
                    signUpUser(email, password, nickname, context)
                }

            }

        }

        return check

    }

}