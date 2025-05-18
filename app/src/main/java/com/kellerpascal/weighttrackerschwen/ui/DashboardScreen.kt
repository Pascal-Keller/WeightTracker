package com.kellerpascal.weighttrackerschwen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kellerpascal.weighttrackerschwen.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    val currentUserEntries by viewModel.weightEntries.collectAsState(initial = emptyList())
    val currentUserPercentage by viewModel.currentUserPercentage.collectAsState(initial = 0f)

    var showAddEntryDialog by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Loss Challenge") },
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEntryDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Weight Entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Users competition section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Competition",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    users.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            if (user.isWinning) {
                                Text(
                                    text = "👑",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            val percentage = if (user.initialWeight > 0) {
                                (user.initialWeight - user.currentWeight) / user.initialWeight * 100
                            } else 0f

                            Text(
                                text = String.format("%.1f%%", percentage),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val progressPercentage = if (user.initialWeight > 0) {
                            (user.initialWeight - user.currentWeight) / user.initialWeight * 100
                        } else 0f

                        // Progress bar
                        LinearProgressIndicator(
                            progress = progressPercentage / 100f * 3f, // Scale to make progress visible (30% loss = full bar)
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            // Your progress section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = String.format("Weight Loss: %.1f%%", currentUserPercentage),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Weight History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(currentUserEntries) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date(entry.date)),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = String.format("%.1f kg", entry.weight),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        if (showAddEntryDialog) {
            Dialog(onDismissRequest = { showAddEntryDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Add Weight Entry",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = newWeight,
                            onValueChange = { newWeight = it },
                            label = { Text("Current Weight (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddEntryDialog = false }) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            val weight = newWeight.toFloatOrNull()
                                                ?: throw Exception("Please enter a valid weight")
                                            viewModel.addWeightEntry(weight)
                                            showAddEntryDialog = false
                                            newWeight = ""
                                            errorMessage = null
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading && newWeight.isNotBlank()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}