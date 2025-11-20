package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentLoginPageBinding
import com.example.chatapp.util.hasher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginPage : Fragment() { // Fragment'ı bu şekilde tanımlayın

    private lateinit var binding: FragmentLoginPageBinding // lateinit burada kalabilir

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.test.setOnClickListener {

            val email = "patates@example.com"
            val password = "123456"
            val nickname = "patates"

            val email2 = "patates2@example.com"
            val password2 = "123456"
            val nickname2 = "patates2"

            if(nickname.contains(" ")){
                Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.nicknamesBlank),
                    Toast.LENGTH_SHORT).show()
            }
            else {

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Başarılı", Toast.LENGTH_SHORT).show()
                        FirebaseFirestore.getInstance().collection("users").document("userMap").set(mapOf(hasher(email.toString()) to nickname),
                            SetOptions.merge())
                        findNavController().popBackStack()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Başarısız ${it}", Toast.LENGTH_SHORT).show()
                    }
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email2, password2)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Başarılı", Toast.LENGTH_SHORT).show()
                        FirebaseFirestore.getInstance().collection("users").document("userMap").set(mapOf(hasher(email2.toString()) to nickname2),
                            SetOptions.merge())
                        findNavController().popBackStack()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Başarısız ${it}", Toast.LENGTH_SHORT).show()
                    }

            }
        }

        binding.girisYap.setOnClickListener {
            val eMail = binding.eMailTextField.text.toString().trim()
            val password = binding.passwordTextField.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(eMail, password).addOnSuccessListener {
                Toast.makeText(requireContext(), "Başarılı", Toast.LENGTH_SHORT).show()
                findNavController().navigate(LoginPageDirections.loginToMain())
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Başarısız ${it}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.createAccount.setOnClickListener {
            findNavController().navigate(LoginPageDirections.loginToSign())
        }
    }
}