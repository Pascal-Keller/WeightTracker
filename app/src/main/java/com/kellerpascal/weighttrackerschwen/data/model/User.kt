package com.kellerpascal.weighttrackerschwen.data.model

data class User(
                val id: String = "",
                val name: String = "",
                val initialWeight: Float = 0f,
                val currentWeight: Float = 0f,
                val isWinning: Boolean = false
) {
    // Empty constructor for Firebase
    constructor() : this("", "", 0f, 0f, false)
}
