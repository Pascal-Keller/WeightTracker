package com.kellerpascal.weighttrackerschwen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kellerpascal.weighttrackerschwen.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onInitialWeightChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.initialWeight,
            onValueChange = onInitialWeightChange,
            label = { Text("Starting Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading &&
                    uiState.email.isNotBlank() &&
                    uiState.password.isNotBlank() &&
                    uiState.name.isNotBlank() &&
                    uiState.initialWeight.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var uiState by remember { mutableStateOf(RegisterUiState()) }
    val scope = rememberCoroutineScope()

    RegisterScreenContent(
        uiState = uiState,
        onEmailChange = { uiState = uiState.copy(email = it) },
        onPasswordChange = { uiState = uiState.copy(password = it) },
        onNameChange = { uiState = uiState.copy(name = it) },
        onInitialWeightChange = { uiState = uiState.copy(initialWeight = it) },
        onSubmit = {
            scope.launch {
                uiState = uiState.copy(isLoading = true, errorMessage = null)
                try {
                    val weight = uiState.initialWeight.toFloatOrNull()
                        ?: throw Exception("Please enter a valid weight")
                    viewModel.register(
                        uiState.email,
                        uiState.password,
                        uiState.name,
                        weight
                    )
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                } catch (e: Exception) {
                    uiState = uiState.copy(errorMessage = e.message)
                } finally {
                    uiState = uiState.copy(isLoading = false)
                }
            }
        },
        onNavigateToLogin = {
            navController.navigate("login")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreenContent(
        uiState = RegisterUiState(),
        onEmailChange = {},
        onPasswordChange = {},
        onNameChange = {},
        onInitialWeightChange = {},
        onSubmit = {},
        onNavigateToLogin = {}
    )
}
