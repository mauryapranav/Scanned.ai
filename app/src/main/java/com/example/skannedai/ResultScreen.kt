package com.example.skannedai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skannedai.viewmodel.ScanUiState
import com.example.skannedai.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, vm: ScanViewModel) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result") },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.reset()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ScanUiState.Processing -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Processing with AI...")
                    }
                }
                is ScanUiState.Success -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text((uiState as ScanUiState.Success).result, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is ScanUiState.Error -> {
                    Text((uiState as ScanUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                else -> { Text("No result yet") }
            }
        }
    }
}