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

        binding.girisYap.setOnClickListener {
            binding.girisYap.isEnabled = false
            binding.girisYap.alpha = 0.5f
            val eMail = binding.eMailTextField.text.toString().trim()
            val password = binding.passwordTextField.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(eMail, password).addOnSuccessListener {
                Toast.makeText(requireContext(), "Başarılı", Toast.LENGTH_SHORT).show()
                binding.girisYap.isEnabled = true
                binding.girisYap.alpha = 1f
                findNavController().navigate(LoginPageDirections.loginToMain())
            }.addOnFailureListener {
                binding.girisYap.isEnabled = true
                binding.girisYap.alpha = 1f
                Toast.makeText(requireContext(), "Başarısız ${it}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.createAccount.setOnClickListener {
            findNavController().navigate(LoginPageDirections.loginToSign())
        }
    }
}