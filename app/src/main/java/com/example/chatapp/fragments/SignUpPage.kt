package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSignUpPageBinding
import com.example.chatapp.util.hasher
import com.example.chatapp.viewmodels.SignUpViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpPage : Fragment() {

    private lateinit var binding: FragmentSignUpPageBinding
    private val signUpPageViewModel: SignUpViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpPageBinding.inflate(inflater, container, false)

        binding.girisYap.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.createAccountSignUp.setOnClickListener {

            val email = binding.eMailTextField.text.toString().trim()
            val password = binding.passwordTextField.text.toString()
            val nickname = binding.nicknameTextField.text.toString()

            signUpPageViewModel.checkAndSignUserUp(email, password, nickname, requireContext())

        }

        binding.test.setOnClickListener {
            val email = "patates@example.com"
            val password = "123456"
            val nickname = "patates"

            val email2 = "patates2@example.com"
            val password2 = "123456"
            val nickname2 = "patates2"
            signUpPageViewModel.checkAndSignUserUp(email, password, nickname, requireContext())
            signUpPageViewModel.checkAndSignUserUp(email2, password2, nickname2, requireContext())
        }

        return binding.root
    }

}