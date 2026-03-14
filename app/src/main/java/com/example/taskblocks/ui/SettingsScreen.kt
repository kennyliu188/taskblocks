package com.example.taskblocks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import com.example.taskblocks.data.Category
import com.example.taskblocks.ui.theme.NavBrown
import com.example.taskblocks.ui.theme.WarmBackground

private val PRESET_COLORS = listOf(
    0xFF4CAF50.toInt(),
    0xFF2196F3.toInt(),
    0xFFFF9800.toInt(),
    0xFF9C27B0.toInt(),
    0xFFF44336.toInt(),
    0xFF00BCD4.toInt(),
    0xFF795548.toInt(),
    0xFF607D8B.toInt(),
    0xFF009688.toInt(),
    0xFF3F51B5.toInt(),
    0xFFFFC107.toInt(),
    0xFFE91E63.toInt(),
    0xFF8BC34A.toInt(),
    0xFF673AB7.toInt(),
    0xFF03A9F4.toInt(),
    0xFFFF5722.toInt()
)

@Composable
fun SettingsScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<Category?>(null) }
    var deleteConfirm by remember { mutableStateOf<Category?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbar.showSnackbar(msg)
            snackbarMessage = null
        }
    }

    Scaffold(
        modifier = modifier.background(WarmBackground),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.category_management),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.category_management),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(cat.colorInt), RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { editCategory = cat }) { Text(stringResource(R.string.edit)) }
                            TextButton(onClick = { deleteConfirm = cat }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Text(stringResource(R.string.new_category_btn))
                    }
                }
            }
        }
    }

    val usedColorIntsAdd = remember(categories) {
        categories.map { it.colorInt }.toSet()
    }
    val usedColorIntsEdit = remember(categories, editCategory) {
        editCategory?.let { cur ->
            categories.filter { it.id != cur.id }.map { it.colorInt }.toSet()
        } ?: emptySet()
    }
    if (showAddDialog) {
        CategoryEditDialog(
            category = null,
            usedColorInts = usedColorIntsAdd,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorInt ->
                viewModel.addCategory(name, colorInt)
                showAddDialog = false
            }
        )
    }
    editCategory?.let { cat ->
        CategoryEditDialog(
            category = cat,
            usedColorInts = usedColorIntsEdit,
            onDismiss = { editCategory = null },
            onConfirm = { name, colorInt ->
                viewModel.updateCategory(cat.copy(name = name, colorInt = colorInt))
                editCategory = null
            }
        )
    }
    deleteConfirm?.let { cat ->
        val deletedMsg = stringResource(R.string.category_deleted)
        val hasTasksMsg = stringResource(R.string.category_has_tasks)
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text(stringResource(R.string.delete_category)) },
            text = { Text(stringResource(R.string.confirm_delete_category, cat.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat.id,
                        onSuccess = { deleteConfirm = null; snackbarMessage = deletedMsg },
                        onFailure = { snackbarMessage = hasTasksMsg }
                    )
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CategoryEditDialog(
    category: Category?,
    usedColorInts: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorInt: Int) -> Unit
) {
    val selectableColors = remember(usedColorInts, category) {
        PRESET_COLORS.filter { c -> c !in usedColorInts || (category != null && category.colorInt == c) }
            .let { list ->
                if (category != null && category.colorInt !in list && category.colorInt !in PRESET_COLORS) {
                    listOf(category.colorInt) + list
                } else list
            }
    }
    val initialColor = category?.colorInt ?: selectableColors.firstOrNull() ?: PRESET_COLORS.first()
    var name by remember(category?.id) { mutableStateOf(category?.name ?: "") }
    var colorInt by remember(category?.id) { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (category != null) R.string.edit_category else R.string.add_category)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.color), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectableColors.chunked(4).forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowColors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(c), CircleShape)
                                        .clickable { colorInt = c },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorInt == c) {
                                        Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name.trim(), colorInt)
            }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
