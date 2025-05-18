package com.kellerpascal.weighttrackerschwen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kellerpascal.weighttrackerschwen.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    val users = repository.users
    val weightEntries = repository.currentUserEntries

    private val _currentUserPercentage = MutableStateFlow(0f)
    val currentUserPercentage = _currentUserPercentage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUserId()?.let { userId ->
                _currentUserPercentage.value = repository.getWeightLossPercentage(userId)
            }
        }
    }

    suspend fun addWeightEntry(weight: Float) {
        repository.addWeightEntry(weight)

        repository.getCurrentUserId()?.let { userId ->
            _currentUserPercentage.value = repository.getWeightLossPercentage(userId)
        }
    }

    fun signOut() {
        repository.signOut()
    }
}
