package com.kellerpascal.weighttrackerschwen.ui

import com.kellerpascal.weighttrackerschwen.data.model.User
import com.kellerpascal.weighttrackerschwen.data.model.WeightEntry


data class DashboardUiState(
    val users: List<User> = emptyList(),
    val currentUserEntries: List<WeightEntry> = emptyList(),
    val currentUserPercentage: Float = 0f,
    val showAddEntryDialog: Boolean = false,
    val newWeight: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
