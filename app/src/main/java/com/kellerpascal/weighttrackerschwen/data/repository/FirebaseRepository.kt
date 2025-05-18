package com.kellerpascal.weighttrackerschwen.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kellerpascal.weighttrackerschwen.data.model.User
import com.kellerpascal.weighttrackerschwen.data.model.WeightEntry
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.Date


class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: Flow<List<User>> = _users.asStateFlow()

    private val _currentUserEntries = MutableStateFlow<List<WeightEntry>>(emptyList())
    val currentUserEntries: Flow<List<WeightEntry>> = _currentUserEntries.asStateFlow()

    init {
        // Listen for users collection changes
        db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val usersList = snapshot?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList()

                _users.value = usersList
                updateWinningStatus()
            }

        // Listen for current user's weight entries
        getCurrentUserId()?.let { userId ->
            db.collection("weightEntries")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("FirebaseRepository", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    val entries = snapshot?.documents?.mapNotNull {
                        it.toObject(WeightEntry::class.java)
                    } ?: emptyList()

                    _currentUserEntries.value = entries
                }
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun registerUser(email: String, password: String, name: String, initialWeight: Float): String {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

        val user = User(
            id = userId,
            name = name,
            initialWeight = initialWeight,
            currentWeight = initialWeight
        )

        db.collection("users").document(userId).set(user).await()

        // Add first weight entry
        addWeightEntry(initialWeight)

        return userId
    }

    suspend fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun addWeightEntry(weight: Float) {
        val userId = getCurrentUserId() ?: throw Exception("User not logged in")

        val entry = WeightEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = Date().time,
            weight = weight
        )

        // Add weight entry
        db.collection("weightEntries").document(entry.id).set(entry).await()

        // Update user's current weight
        db.collection("users").document(userId)
            .update("currentWeight", weight)
            .await()

        updateWinningStatus()
    }

    private fun updateWinningStatus() {
        val usersList = _users.value
        if (usersList.size < 2) return

        // Calculate weight loss percentages
        val usersWithPercentage = usersList.map { user ->
            val weightLossPercentage = if (user.initialWeight > 0) {
                (user.initialWeight - user.currentWeight) / user.initialWeight * 100
            } else 0f

            user to weightLossPercentage
        }

        // Find the user with the highest weight loss percentage
        val maxPercentageUser = usersWithPercentage.maxByOrNull { it.second }?.first ?: return

        // Update winning status for all users
        usersList.forEach { user ->
            val isWinning = user.id == maxPercentageUser.id
            if (user.isWinning != isWinning) {
                db.collection("users").document(user.id)
                    .update("isWinning", isWinning)
            }
        }
    }

    suspend fun getWeightLossPercentage(userId: String): Float {
        val userDoc = db.collection("users").document(userId).get().await()
        val user = userDoc.toObject(User::class.java) ?: return 0f

        return if (user.initialWeight > 0) {
            (user.initialWeight - user.currentWeight) / user.initialWeight * 100
        } else 0f
    }

    fun signOut() {
        auth.signOut()
    }
}