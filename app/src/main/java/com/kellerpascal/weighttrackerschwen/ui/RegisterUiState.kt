package com.kellerpascal.weighttrackerschwen.ui

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val initialWeight: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
