package com.example.taskblocks.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import com.example.taskblocks.data.Task
import com.example.taskblocks.ui.theme.NavBrown
import com.example.taskblocks.ui.theme.WarmSurface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun KanbanScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    onTaskClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedTasks by viewModel.completedTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val colorMap = remember(categories) {
        categories.associate { it.id to Color(it.colorInt) }
    }

    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val tasksInMonth = remember(completedTasks, selectedYear, selectedMonth) {
        completedTasks.filter { task ->
            task.completedAt?.let { at ->
                val c = Calendar.getInstance().apply { timeInMillis = at }
                c.get(Calendar.YEAR) == selectedYear && c.get(Calendar.MONTH) + 1 == selectedMonth
            } ?: false
        }
    }

    val byDay = remember(tasksInMonth) {
        tasksInMonth.groupBy { task ->
            task.completedAt?.let { at ->
                Calendar.getInstance().apply { timeInMillis = at }.get(Calendar.DAY_OF_MONTH)
            } ?: 0
        }.toSortedMap(reverseOrder())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedIds = emptySet()
                        } else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    SquareCheckIcon(
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(if (isSelectionMode) R.string.select_tasks else R.string.completed_kanban),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSelectionMode) {
                        Text(
                            text = stringResource(R.string.select_all),
                            style = MaterialTheme.typography.bodyLarge,
                            color = NavBrown,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    val allIds = tasksInMonth.map { it.id }.toSet()
                                    selectedIds = if (selectedIds.size == allIds.size) emptySet() else allIds
                                }
                        )
                        IconButton(
                            onClick = {
                                if (selectedIds.isNotEmpty()) showDeleteConfirm = true
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == 1) {
                            selectedMonth = 12
                            selectedYear--
                        } else selectedMonth--
                    }) {
                        Text("<", style = MaterialTheme.typography.titleMedium, color = NavBrown)
                    }
                    Text(
                        text = "$selectedYear-$selectedMonth",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = {
                        if (selectedMonth == 12) {
                            selectedMonth = 1
                            selectedYear++
                        } else selectedMonth++
                    }) {
                        Text(">", style = MaterialTheme.typography.titleMedium, color = NavBrown)
                    }
                }
            }
            if (byDay.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_completed_this_month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                val dayEntries = byDay.entries.toList()
                items(dayEntries.size, key = { dayEntries[it].key }) { index ->
                    val (day, dayTasks) = dayEntries[index]
                    val tasks = dayTasks.sortedByDescending { it.completedAt ?: 0L }
                    val cal = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth - 1, day)
                    }
                    val weekday = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$weekday $day",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                        tasks.forEach { task ->
                            CompletedTaskRow(
                                task = task,
                                colorMap = colorMap,
                                isSelectionMode = isSelectionMode,
                                isSelected = task.id in selectedIds,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedIds = if (task.id in selectedIds) selectedIds - task.id
                                        else selectedIds + task.id
                                    } else onTaskClick(task.id)
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedIds = setOf(task.id)
                                    }
                                },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.confirm_delete)) },
                text = { Text(stringResource(R.string.confirm_delete_completed, selectedIds.size)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCompletedTasks(selectedIds.toList())
                        selectedIds = emptySet()
                        isSelectionMode = false
                        showDeleteConfirm = false
                    }) {
                        Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
private fun SquareCheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(1.5.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.minDimension
            val pad = w * 0.25f
            val path = Path().apply {
                moveTo(pad, w * 0.5f)
                lineTo(w * 0.42f, w * 0.68f)
                lineTo(w - pad, w * 0.32f)
            }
            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun CompletedTaskRow(
    task: Task,
    colorMap: Map<Long, Color>,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeStr = task.completedAt?.let { timeFormat.format(it) } ?: "--:--"
    val blockColor = colorMap[task.categoryId] ?: Color(0xFFD5943B)
    val stars = task.completionStars ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = timeStr,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 4.dp, bottomEnd = 4.dp))
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 4.dp, bottomEnd = 4.dp))
                .background(blockColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
                if (stars in 1..3 && !isSelectionMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(stars) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
