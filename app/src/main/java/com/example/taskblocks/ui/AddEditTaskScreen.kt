package com.example.taskblocks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import com.example.taskblocks.data.Category
import com.example.taskblocks.data.ImportanceLevel
import com.example.taskblocks.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    existingTask: Task?,
    categories: List<Category>,
    onSave: (title: String, description: String, importance: Int, categoryId: Long, deadlineAt: Long?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(existingTask?.id) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask?.id) { mutableStateOf(existingTask?.description ?: "") }
    var importance by remember(existingTask?.id) { mutableIntStateOf(existingTask?.importance ?: 1) }
    var categoryId by remember(existingTask?.id) {
        mutableStateOf(existingTask?.categoryId ?: (categories.firstOrNull()?.id ?: 0L))
    }
    var hasDeadline by remember(existingTask?.id) {
        mutableStateOf(existingTask?.deadlineAt != null)
    }
    var deadlineAt by remember(existingTask?.id) {
        mutableStateOf(existingTask?.deadlineAt)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (deadlineAt ?: System.currentTimeMillis()).let { ms ->
            java.util.Calendar.getInstance().apply { timeInMillis = ms }.apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    )
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && categoryId == 0L) categoryId = categories.first().id
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(if (existingTask != null) R.string.edit_task else R.string.add_task),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.description_optional)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.importance_quadrant),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ImportanceLevel.LOW,   // 不重要不紧急
                    ImportanceLevel.MEDIUM // 不重要但紧急
                ).forEach { level ->
                    FilterChip(
                        selected = importance == level.level,
                        onClick = { importance = level.level },
                        label = { Text(stringResource(level.displayNameResId)) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ImportanceLevel.HIGH,   // 重要但不紧急
                    ImportanceLevel.URGENT  // 重要而且紧急
                ).forEach { level ->
                    FilterChip(
                        selected = importance == level.level,
                        onClick = { importance = level.level },
                        label = { Text(stringResource(level.displayNameResId)) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val baseColor = androidx.compose.ui.graphics.Color(cat.colorInt)
                FilterChip(
                    selected = categoryId == cat.id,
                    onClick = { categoryId = cat.id },
                    label = { Text(cat.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = baseColor.copy(alpha = 0.18f),
                        labelColor = MaterialTheme.colorScheme.onBackground,
                        selectedContainerColor = baseColor,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = hasDeadline,
                onCheckedChange = {
                    hasDeadline = it
                    if (!it) deadlineAt = null
                    else if (deadlineAt == null) deadlineAt = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            )
            Text(
                text = stringResource(R.string.set_deadline),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (hasDeadline) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                Text(
                    text = if (deadlineAt != null) fmt.format(java.util.Date(deadlineAt!!))
                    else stringResource(R.string.deadline)
                )
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { ms ->
                            val cal = java.util.Calendar.getInstance()
                            cal.timeInMillis = ms
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            deadlineAt = cal.timeInMillis
                        }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.confirm)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (title.isNotBlank() && categoryId != 0L) {
                        onSave(title.trim(), description.trim(), importance, categoryId, if (hasDeadline) deadlineAt else null)
                    }
                }
            ) { Text(stringResource(if (existingTask != null) R.string.save else R.string.add)) }
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
        }
    }
}
