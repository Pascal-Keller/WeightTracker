package com.kellerpascal.weighttrackerschwen.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.kellerpascal.weighttrackerschwen.data.repository.FirebaseRepository

class AuthViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    suspend fun register(email: String, password: String, name: String, initialWeight: Float) {
        repository.registerUser(email, password, name, initialWeight)
    }

    suspend fun login(email: String, password: String) {
        repository.loginUser(email, password)
    }
}