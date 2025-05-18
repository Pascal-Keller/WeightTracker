package com.kellerpascal.weighttrackerschwen.data.model

data class WeightEntry(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val weight: Float = 0f
) {
    // Empty constructor for Firebase
    constructor() : this(
        "",
        "",
        0L,
        0f
    )
}