package com.example.taskblocks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskblocks.R
import com.example.taskblocks.data.Category
import com.example.taskblocks.data.ImportanceLevel
import com.example.taskblocks.data.Record
import com.example.taskblocks.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val recordTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
private fun DetailRow(
    label: String,
    content: String,
    contentFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    contentOnNewLine: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = contentFontSize),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (contentOnNewLine) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TaskDetailScreen(
    taskId: Long,
    viewModel: TaskViewModel,
    categories: List<Category>,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    onCompleteAndBack: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onRecordItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
        viewModel.setDetailTaskId(taskId)
    }
    val currentTask by viewModel.selectedTask.collectAsState()
    val records by viewModel.taskRecords.collectAsState()
    if (currentTask == null) {
        Text(stringResource(R.string.loading), modifier = Modifier.padding(16.dp))
        return
    }
    val task = currentTask!!

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.task_detail), style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp))
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                DetailRow(label = stringResource(R.string.title), content = task.title, contentFontSize = 18.sp, contentOnNewLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = stringResource(R.string.importance), content = stringResource(ImportanceLevel.fromLevel(task.importance).displayNameResId))
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = stringResource(R.string.category), content = categories.find { it.id == task.categoryId }?.name ?: "")
                if (task.deadlineAt != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    DetailRow(label = stringResource(R.string.deadline), content = fmt.format(java.util.Date(task.deadlineAt!!)))
                }
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow(label = stringResource(R.string.description), content = task.description, contentOnNewLine = true)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onEdit(task.id) },
                modifier = Modifier.height(44.dp)
            ) { Text(stringResource(R.string.edit), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)) }
            OutlinedButton(
                onClick = { onRecordClick(taskId) },
                modifier = Modifier.height(44.dp)
            ) { Text(stringResource(R.string.record), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)) }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(44.dp)
            ) { Text(stringResource(R.string.back), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)) }
        }
        if (records.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.record), style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), modifier = Modifier.padding(bottom = 8.dp))
            records.forEach { record ->
                RecordSummaryRow(
                    record = record,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onRecordItemClick(record.id) }
                )
            }
        }
        }
    }
}

@Composable
private fun RecordSummaryRow(
    record: Record,
    modifier: Modifier = Modifier
) {
    val timeStr = recordTimeFormat.format(Date(record.createdAt))
    val brief = record.content.take(20).let { if (record.content.length > 20) "$it…" else it }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(timeStr, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(brief.ifEmpty { stringResource(R.string.record_brief_empty) }, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), maxLines = 1)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (record.hasImage()) {
                Text(
                    text = stringResource(R.string.record_image_tag),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
            }
            if (record.hasDocument()) {
                Text(
                    text = stringResource(R.string.record_doc_tag),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
            }
        }
    }
}
