package com.example.taskblocks.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import com.example.taskblocks.ui.theme.NavBrown
import com.example.taskblocks.ui.theme.WarmBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DataManagementScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val totalTasks = activeTasks.size + completedTasks.size
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val backupSuccessMsg = stringResource(R.string.backup_success)
    val backupFailedFormat = stringResource(R.string.backup_failed)
    val cannotReadFileMsg = stringResource(R.string.cannot_read_file)
    val importSuccessMsg = stringResource(R.string.import_success)
    val importFormatErrorMsg = stringResource(R.string.import_format_error)
    val importFailedFormat = stringResource(R.string.import_failed)
    val dataClearedMsg = stringResource(R.string.data_cleared)
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }

    fun backupFileName(): String {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "taskblocks${fmt.format(cal.time)}.zip"
    }

    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        isExporting = false
        uri?.let { u ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(u)?.use { os ->
                            viewModel.exportToZip(os)
                        }
                        snackbar.showSnackbar(backupSuccessMsg)
                    } catch (e: Exception) {
                        snackbar.showSnackbar(backupFailedFormat.format(e.message.orEmpty()))
                    }
                }
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        isImporting = false
        uri?.let { u ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val input = context.contentResolver.openInputStream(u)
                        if (input == null) {
                            snackbar.showSnackbar(cannotReadFileMsg)
                        } else {
                            input.use {
                                viewModel.importFromStream(it)
                                    .onSuccess { snackbar.showSnackbar(importSuccessMsg) }
                                    .onFailure { snackbar.showSnackbar(importFormatErrorMsg) }
                            }
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            snackbar.showSnackbar(importFailedFormat.format(e.message.orEmpty()))
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", style = MaterialTheme.typography.titleLarge, color = NavBrown)
                }
                Text(
                    text = stringResource(R.string.data_management),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.task_stats),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StatRow(stringResource(R.string.total_tasks), totalTasks)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow(stringResource(R.string.active_tasks), activeTasks.size)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow(stringResource(R.string.completed_tasks), completedTasks.size)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.backup_import),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isExporting = true
                                    createDocument.launch(backupFileName())
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isExporting
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.backup))
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    isImporting = true
                                    openDocument.launch(arrayOf("application/zip", "application/json", "*/*"))
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isImporting
                            ) {
                                if (isImporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.import_data))
                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.backup_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showClearDataConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.clear_data))
                        }
                    }
                }
            }
        }
        if (showClearDataConfirm) {
            AlertDialog(
                onDismissRequest = { showClearDataConfirm = false },
                title = { Text(stringResource(R.string.confirm_clear)) },
                text = { Text(stringResource(R.string.confirm_clear_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                viewModel.clearAllData()
                            }
                            showClearDataConfirm = false
                            snackbar.showSnackbar(dataClearedMsg)
                        }
                    }) {
                        Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun StatRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.count_units, count),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
