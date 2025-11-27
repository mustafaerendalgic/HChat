package com.example.chatapp.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var uri: Uri? = null

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

        val imageChoosingLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri1 ->

            if (uri1 != null){
                uri = uri1
                binding.profilePicture.setImageURI(uri)
            }

        }

        binding.createAccountSignUp.setOnClickListener {

            if(uri == null){
                Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.putProfilePicture),
                    Toast.LENGTH_SHORT).show()
            }
            else {
                binding.createAccountSignUp.isEnabled = false
                binding.createAccountSignUp.alpha = 0.5f

                val email = binding.eMailTextField.text.toString().trim()
                val password = binding.passwordTextField.text.toString()
                val nickname = binding.nicknameTextField.text.toString()

                if(email.isBlank() || password.isBlank() || nickname.isBlank()){
                    Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.isBlank),
                        Toast.LENGTH_SHORT).show()
                    binding.createAccountSignUp.isEnabled = true
                    binding.createAccountSignUp.alpha = 1f
                }

                else{
                    signUpPageViewModel.checkAndSignUserUp(
                        email, password, nickname, requireContext(), binding.createAccountSignUp, uri!!)
                }

            }

        }

        binding.profilePicture.setOnClickListener {

            imageChoosingLauncher.launch("image/*")

        }

        return binding.root
    }

}