package com.example.skannedai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun ScanScreen(navController: NavController, vm: ScanViewModel) {
    val uiState by vm.uiState.collectAsState()
    val extractedText by vm.extractedText.collectAsState()
    var selectedMode by remember { mutableStateOf("summarize") }
    var question by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.extractTextFromUri(it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Processing || uiState is ScanUiState.Success) {
            navController.navigate("result")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Scan AI") },
                actions = {
                    TextButton(onClick = { navController.navigate("history") }) {
                        Text("History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Image from Gallery")
            }

            if (extractedText.isNotBlank()) {
                Text("Extracted Text:", style = MaterialTheme.typography.labelLarge)
                Text(
                    extractedText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Text("Select Mode:", style = MaterialTheme.typography.labelLarge)
            val modes = listOf("summarize", "flashcards", "exam_questions", "ask")
            modes.forEach { mode ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMode == mode, onClick = { selectedMode = mode })
                    Text(mode)
                }
            }

            if (selectedMode == "ask") {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Your question") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { vm.processText(selectedMode, question) },
                modifier = Modifier.fillMaxWidth(),
                enabled = extractedText.isNotBlank() && uiState !is ScanUiState.Processing
            ) {
                Text("Process")
            }

            if (uiState is ScanUiState.ExtractingText) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Extracting text...", modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (uiState is ScanUiState.Error) {
                Text((uiState as ScanUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}