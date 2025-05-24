package com.kellerpascal.weighttrackerschwen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kellerpascal.weighttrackerschwen.data.model.User
import com.kellerpascal.weighttrackerschwen.data.model.WeightEntry
import com.kellerpascal.weighttrackerschwen.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    state: DashboardUiState,
    onWeightChange: (String) -> Unit,
    onAddEntry: () -> Unit,
    onSaveEntry: () -> Unit,
    onCancelEntry: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Loss Challenge") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry) {
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
            // Competition
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Competition", style = MaterialTheme.typography.titleLarge)
                    state.users.forEach { user ->
                        val percentage = if (user.initialWeight > 0) {
                            if (user.currentWeight < user.initialWeight) {
                                // weight loss -> negative percentage
                                -((user.initialWeight - user.currentWeight) / user.initialWeight * 100)
                            } else {
                                // weight gain -> positive percentage
                                ((user.currentWeight - user.initialWeight) / user.initialWeight * 100)
                            }
                        } else 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(user.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            if (user.isWinning) Text("👑", style = MaterialTheme.typography.headlineMedium)
                            Text(String.format(Locale.getDefault(), "%.1f%%", percentage), color = MaterialTheme.colorScheme.primary)
                        }
                        LinearProgressIndicator(
                            progress = { (percentage / 100f * 3f).coerceAtMost(1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                    }
                }
            }

            // Your progress
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Progress", style = MaterialTheme.typography.titleLarge)
                    Text(
                        String.format(Locale.getDefault(), "Weight Loss: %.1f%%", state.currentUserPercentage),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Weight History", style = MaterialTheme.typography.titleMedium)

                    LazyColumn {
                        items(state.currentUserEntries) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.date)))
                                Text("${entry.weight} kg", fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // Dialog
        if (state.showAddEntryDialog) {
            Dialog(onDismissRequest = onCancelEntry) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Weight Entry", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = state.newWeight,
                            onValueChange = onWeightChange,
                            label = { Text("Current Weight (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        state.errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onCancelEntry) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onSaveEntry,
                                enabled = !state.isLoading && state.newWeight.isNotBlank()
                            ) {
                                if (state.isLoading) {
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


@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    val entries by viewModel.weightEntries.collectAsState(initial = emptyList())
    val percentage by viewModel.currentUserPercentage.collectAsState(initial = 0f)

    var state by remember {
        mutableStateOf(DashboardUiState(users, entries, percentage))
    }
    val scope = rememberCoroutineScope()

    DashboardScreenContent(
        state = state,
        onWeightChange = { state = state.copy(newWeight = it) },
        onAddEntry = { state = state.copy(showAddEntryDialog = true) },
        onCancelEntry = { state = state.copy(showAddEntryDialog = false, errorMessage = null) },
        onSaveEntry = {
            scope.launch {
                state = state.copy(isLoading = true)
                try {
                    val weight = state.newWeight.toFloatOrNull()
                        ?: throw Exception("Please enter a valid weight")
                    viewModel.addWeightEntry(weight)
                    state = state.copy(showAddEntryDialog = false, newWeight = "", errorMessage = null)
                } catch (e: Exception) {
                    state = state.copy(errorMessage = e.message)
                } finally {
                    state = state.copy(isLoading = false)
                }
            }
        },
        onSignOut = {
            viewModel.signOut()
            navController.navigate("login") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreenContent(
        state = DashboardUiState(
            users = listOf(
                User("0", "Pascal", 85f, 83f, true),
                User("1", "Schwen", 86f, 85f, false)
            ),
            currentUserEntries = listOf(
                WeightEntry("0", "0", System.currentTimeMillis(), 86f),
                WeightEntry("0", "0", System.currentTimeMillis(), 83f),
            ),
            currentUserPercentage = 12.5f,
//            showAddEntryDialog = true,
        ),
        onWeightChange = {},
        onAddEntry = {},
        onSaveEntry = {},
        onCancelEntry = {},
        onSignOut = {}
    )
}
