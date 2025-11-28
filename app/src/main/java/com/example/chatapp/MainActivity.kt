package com.example.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment?

        if (navHostFragment != null) {

            val navController = navHostFragment.navController

            val auth = FirebaseAuth.getInstance()

            val authStateListener = FirebaseAuth.AuthStateListener { authState ->
                val user = authState.currentUser
                if (user != null) {
                    when(navController.currentDestination?.id){
                        R.id.loginPage -> navController.navigate(R.id.loginToMain)
                        R.id.signUpPage -> navController.navigate(R.id.signToLogin)
                    }
                }
                else{
                    when(navController.currentDestination?.id){
                        R.id.mainPage -> navController.navigate(R.id.mainToLogin)
                        R.id.chatPage -> navController.navigate(R.id.chatToLogin)
                    }
                }
            }

            auth.addAuthStateListener(authStateListener)


        } else {
            Log.e("MainActivity", "NavHostFragment not found!")
        }


    }
}