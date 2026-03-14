package com.example.taskblocks.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.taskblocks.BuildConfig
import com.example.taskblocks.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import android.util.Log
import com.example.taskblocks.ui.theme.NavBrown
import com.example.taskblocks.ui.theme.WarmOutline
import com.example.taskblocks.ui.theme.WarmSurface
import com.example.taskblocks.data.BlockStyle
import com.example.taskblocks.data.Category
import com.example.taskblocks.data.GRID_COLS
import com.example.taskblocks.data.ImportanceLevel
import com.example.taskblocks.data.GRID_ROWS
import com.example.taskblocks.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.min

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onTaskClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onKanbanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.activeTasks.collectAsState()
    val blockStyle by viewModel.blockStyle.collectAsState()
    val gridFullMessage by viewModel.gridFullMessage.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectedTaskForMenu by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(gridFullMessage) {
        gridFullMessage?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.clearGridFullMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.home_logo_cd),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (BuildConfig.SHOW_ADS) {
                    HomeBannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        adUnitId = BuildConfig.ADMOB_HOME_BANNER_ID
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(NavBrown)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    KanbanNavButton(
                        onClick = onKanbanClick,
                        modifier = Modifier.height(40.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val containerPaddingH = 8.dp
            val containerPaddingV = 16.dp
            val availableWidth = maxWidth - containerPaddingH
            val availableHeight = maxHeight - containerPaddingV
            val cellSizeByWidth = availableWidth / GRID_COLS
            val cellSizeByHeight = availableHeight / GRID_ROWS
            val cellSizeDp = if (cellSizeByWidth.value < cellSizeByHeight.value) cellSizeByWidth else cellSizeByHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clipToBounds()
                    .background(WarmSurface)
                    .border(1.dp, WarmOutline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val categories by viewModel.categories.collectAsState()
                val completingTask by viewModel.completingTask.collectAsState()
                val taskForRating by viewModel.taskForRating.collectAsState()
                val gridTasksWithoutCompleting = remember(tasks, completingTask) {
                    if (completingTask == null) tasks
                    else tasks.filter { it.id != completingTask!!.id }
                }
                TaskGrid(
                    tasks = gridTasksWithoutCompleting,
                    categories = categories,
                    cellSizeDp = cellSizeDp,
                    blockStyle = blockStyle,
                    onBlockClick = { task ->
                        selectedTaskForMenu = if (selectedTaskForMenu?.id == task.id) null else task
                    },
                    modifier = Modifier.align(Alignment.TopStart)
                )
                if (selectedTaskForMenu != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .clickable(enabled = true, onClick = { selectedTaskForMenu = null })
                    )
                    BlockFloatingMenu(
                        task = selectedTaskForMenu!!,
                        cellSizeDp = cellSizeDp,
                        onDetail = {
                            val t = selectedTaskForMenu!!
                            selectedTaskForMenu = null
                            onTaskClick(t.id)
                        },
                        onCancel = {
                            val t = selectedTaskForMenu!!
                            selectedTaskForMenu = null
                            viewModel.startDisappearAnimation(t, isComplete = false)
                        },
                        onComplete = {
                            val t = selectedTaskForMenu!!
                            selectedTaskForMenu = null
                            viewModel.showCompletionRating(t)
                        },
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
                if (taskForRating != null) {
                    CompletionRatingOverlay(
                        task = taskForRating!!,
                        categories = categories,
                        cellSizeDp = cellSizeDp,
                        onStarSelected = { stars ->
                            viewModel.startDisappearAnimation(taskForRating!!, isComplete = true, stars = stars)
                        },
                        onDismiss = { viewModel.dismissCompletionRating() },
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
                if (completingTask != null) {
                    CompletingBlockOverlay(
                        task = completingTask!!,
                        categories = categories,
                        cellSizeDp = cellSizeDp,
                        onAnimationEnd = { viewModel.finishDisappearAnimation() },
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskGrid(
    tasks: List<Task>,
    categories: List<Category>,
    cellSizeDp: androidx.compose.ui.unit.Dp,
    blockStyle: BlockStyle,
    onBlockClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorMap = remember(categories) {
        categories.associate { it.id to Color(it.colorInt) }
    }
    val cellPx = with(LocalDensity.current) { cellSizeDp.toPx() }

    Box(modifier = modifier) {
        val gridWidthPx = cellPx * GRID_COLS
        val gridHeightPx = cellPx * GRID_ROWS
        val gridWidthDp = with(LocalDensity.current) { gridWidthPx.toDp() }
        val gridHeightDp = with(LocalDensity.current) { gridHeightPx.toDp() }

        Box(
            modifier = Modifier
                .size(gridWidthDp, gridHeightDp)
                .clipToBounds()
                .background(WarmSurface)
        ) {
            tasks.forEach { task ->
                key(task.id) {
                    val level = task.importanceLevel()
                    val baseW = level.gridWidth
                    val baseH = level.gridHeight
                    val w = if (task.rotated) baseH else baseW
                    val h = if (task.rotated) baseW else baseH
                    val color = colorMap[task.categoryId] ?: Color(0xFF9C27B0)
                    val row = task.gridRow.coerceIn(0, (GRID_ROWS - h).coerceAtLeast(0))
                    val col = task.gridCol.coerceIn(0, GRID_COLS - w)
                    val blockModifier = when (blockStyle) {
                        BlockStyle.DEFAULT -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        BlockStyle.CUBE_3D -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        color,
                                        androidx.compose.ui.graphics.lerp(color, Color.Black, 0.15f),
                                        androidx.compose.ui.graphics.lerp(color, Color.Black, 0.3f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        BlockStyle.METAL -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFB0BEC5).copy(alpha = 0.9f),
                                        Color(0xFF546E7A),
                                        Color(0xFF37474F)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        BlockStyle.WOOD -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF8D6E63),
                                        Color(0xFF5D4037),
                                        Color(0xFF3E2723)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFFA1887F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    }
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (col * cellPx).roundToInt(),
                                    (row * cellPx).roundToInt()
                                )
                            }
                            .size(
                                width = cellSizeDp * w,
                                height = cellSizeDp * h
                            )
                            .padding(4.dp)
                            .then(blockModifier)
                            .clickable { onBlockClick(task) },
                        contentAlignment = Alignment.Center
                    ) {
                        val blockWidthDp = cellSizeDp * w
                        val blockHeightDp = cellSizeDp * h

                        // 方块内只显示首字母/首字：中文取首字，英文取首词首字母大写
                        val blockInitial = getBlockInitial(task.title.ifBlank { " " })

                        val baseFontByWidth = (blockWidthDp.value * 0.35f).coerceIn(12f, 24f)
                        val baseFontByHeight = (blockHeightDp.value * 0.30f).coerceIn(12f, 22f)
                        val fontSize = min(baseFontByWidth, baseFontByHeight).sp
                        val todayStart = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val (daysText, subColor) = if (task.deadlineAt != null) {
                            val daysLeft = ((task.deadlineAt!! - todayStart) / (1000L * 60 * 60 * 24)).toInt()
                            if (daysLeft >= 0) {
                                stringResource(R.string.days_left, daysLeft) to Color.White.copy(alpha = 0.88f)
                            } else {
                                stringResource(R.string.overdue) to Color.White.copy(alpha = 0.95f)
                            }
                        } else {
                            val days = ((System.currentTimeMillis() - task.createdAt) / (1000L * 60 * 60 * 24)).toInt()
                            "$days" to Color.White.copy(alpha = 0.88f)
                        }
                        val subFontSize = (fontSize.value * 0.65f).coerceIn(8f, 14f).sp

                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = blockInitial,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = daysText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = subFontSize),
                                color = subColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockFloatingMenu(
    task: Task,
    cellSizeDp: androidx.compose.ui.unit.Dp,
    onDetail: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellPx = with(LocalDensity.current) { cellSizeDp.toPx() }
    val level = task.importanceLevel()
    val baseW = level.gridWidth
    val baseH = level.gridHeight
    val w = if (task.rotated) baseH else baseW
    val h = if (task.rotated) baseW else baseH
    val row = task.gridRow.coerceIn(0, (GRID_ROWS - h).coerceAtLeast(0))
    val col = task.gridCol.coerceIn(0, GRID_COLS - w)
    val gridWidthDp = with(LocalDensity.current) { (cellPx * GRID_COLS).toDp() }
    val gridHeightDp = with(LocalDensity.current) { (cellPx * GRID_ROWS).toDp() }
    val gridHeightPx = with(LocalDensity.current) { gridHeightDp.toPx() }
    val gridWidthPx = with(LocalDensity.current) { gridWidthDp.toPx() }
    val menuHeightPx = 100f
    val menuWidthPx = (cellPx * 6.5f).coerceAtLeast(180f)
    val spaceBelow = gridHeightPx - ((row + h) * cellPx + 8f)
    val spaceAbove = (row * cellPx) - 8f
    val showAbove = spaceBelow < menuHeightPx && spaceAbove >= menuHeightPx
    val menuOffsetY = if (showAbove) {
        ((row * cellPx) - menuHeightPx - 8f).coerceAtLeast(8f)
    } else {
        (row + h) * cellPx + 8f
    }
    val menuOffsetX = (col * cellPx).coerceIn(
        8f,
        (gridWidthPx - menuWidthPx - 8f).coerceAtLeast(8f)
    )

    Box(
        modifier = modifier
            .size(gridWidthDp, gridHeightDp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        menuOffsetX.roundToInt(),
                        menuOffsetY.roundToInt()
                    )
                }
                .widthIn(max = with(LocalDensity.current) { menuWidthPx.toDp() })
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .border(1.dp, WarmOutline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = task.title.ifBlank { " " },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                OutlinedButton(
                onClick = onDetail,
                modifier = Modifier.height(36.dp)
            ) { Text(stringResource(R.string.detail), style = MaterialTheme.typography.labelMedium) }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.height(36.dp)
            ) { Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelMedium) }
            Button(
                onClick = onComplete,
                modifier = Modifier.height(36.dp)
            ) { Text(stringResource(R.string.complete), style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

private fun getBlockInitial(title: String): String {
    val t = title.trim()
    if (t.isEmpty()) return " "
    val first = t.first()
    return when {
        first in 'a'..'z' || first in 'A'..'Z' -> {
            t.takeWhile { it.isLetter() }.firstOrNull()?.uppercaseChar()?.toString() ?: first.toString()
        }
        else -> first.toString()
    }
}

@Composable
private fun CompletionRatingOverlay(
    task: Task,
    categories: List<Category>,
    cellSizeDp: androidx.compose.ui.unit.Dp,
    onStarSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStars by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {}
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.complete_rating),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..3).forEach { n ->
                        val label = when (n) {
                            1 -> stringResource(R.string.rating_normal)
                            2 -> stringResource(R.string.rating_good)
                            else -> stringResource(R.string.rating_very_good)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedStars = n }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = label,
                                tint = if (selectedStars >= n) Color(0xFFFFB300) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.rating_confirm_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = { if (selectedStars >= 1) onStarSelected(selectedStars) },
                        enabled = selectedStars >= 1
                    ) { Text(stringResource(R.string.confirm)) }
                }
            }
        }
    }
}

private const val FRAGMENT_COLS = 4
private const val FRAGMENT_ROWS = 4
private const val EXPLOSION_DURATION_MS = 500

@Composable
private fun CompletingBlockOverlay(
    task: Task,
    categories: List<Category>,
    cellSizeDp: androidx.compose.ui.unit.Dp,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorMap = remember(categories) { categories.associate { it.id to Color(it.colorInt) } }
    val cellPx = with(LocalDensity.current) { cellSizeDp.toPx() }
    val level = task.importanceLevel()
    val baseW = level.gridWidth
    val baseH = level.gridHeight
    val w = if (task.rotated) baseH else baseW
    val h = if (task.rotated) baseW else baseH
    val color = colorMap[task.categoryId] ?: Color(0xFF9C27B0)
    val row = task.gridRow.coerceIn(0, (GRID_ROWS - h).coerceAtLeast(0))
    val col = task.gridCol.coerceIn(0, GRID_COLS - w)
    val gridWidthDp = with(LocalDensity.current) { (cellPx * GRID_COLS).toDp() }
    val gridHeightDp = with(LocalDensity.current) { (cellPx * GRID_ROWS).toDp() }

    val blockWidthPx = cellPx * w
    val blockHeightPx = cellPx * h
    val fragmentW = blockWidthPx / FRAGMENT_COLS
    val fragmentH = blockHeightPx / FRAGMENT_ROWS
    val centerX = blockWidthPx / 2f
    val centerY = blockHeightPx / 2f
    val explodeDist = maxOf(blockWidthPx, blockHeightPx) * 1.2f

    val fragments = remember {
        (0 until FRAGMENT_COLS).flatMap { ci ->
            (0 until FRAGMENT_ROWS).map { ri ->
                val fx = (ci + 0.5f) * fragmentW
                val fy = (ri + 0.5f) * fragmentH
                val dx = fx - centerX
                val dy = fy - centerY
                val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                Pair(
                    Offset(fx, fy),
                    Offset(dx / len * explodeDist, dy / len * explodeDist)
                )
            }
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(task.id) {
        delay(80)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(EXPLOSION_DURATION_MS, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    Box(
        modifier = modifier.size(gridWidthDp, gridHeightDp),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (col * cellPx).roundToInt(),
                        (row * cellPx).roundToInt()
                    )
                }
                .size(cellSizeDp * w, cellSizeDp * h)
                .padding(2.dp)
        ) {
            fragments.forEachIndexed { idx, (basePos, explodeVec) ->
                key(idx) {
                val p = progress.value
                val offsetX = basePos.x + explodeVec.x * p - fragmentW / 2f
                val offsetY = basePos.y + explodeVec.y * p - fragmentH / 2f
                val scale = (1f - p).coerceIn(0f, 1f)
                val alpha = (1f - p * 1.2f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(with(LocalDensity.current) { (fragmentW / density).dp }, with(LocalDensity.current) { (fragmentH / density).dp })
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha
                        )
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
                }
            }
        }
    }
}

@Composable
private fun KanbanNavButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.kanban_entry),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.95f)
        )
    }
}

@Composable
private fun KanbanCalendarIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.minDimension
        val pad = w * 0.08f
        val topRad = w * 0.12f
        val cellW = (w - pad * 2f) / 3f
        val cellH = (w - pad * 2f - topRad) / 3f
        val gap = w * 0.04f
        for (row in 0..2) {
            for (col in 0..2) {
                val left = pad + col * (cellW + gap)
                val top = pad + topRad + row * (cellH + gap)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(left, top),
                    size = Size(cellW, cellH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f, w * 0.03f)
                )
            }
        }
        drawRoundRect(
            color = tint,
            topLeft = Offset(pad, 0f),
            size = Size(w - pad * 2f, topRad + pad),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(topRad, topRad),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.06f)
        )
    }
}

private val LogoPurple = Color(0xFF9B7BDB)
private val LogoGreen = Color(0xFF4CAF50)
private val LogoBlue = Color(0xFF4A90E2)
private val LogoOrange = Color(0xFFFF9800)

@Composable
private fun AppLogoIcon(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(LogoPurple)
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(LogoGreen)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(LogoBlue)
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(LogoOrange)
            )
        }
    }
}

private const val AD_LOG_TAG = "TaskBlocks_Ad"

@Composable
private fun HomeBannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(AD_LOG_TAG, "Banner ad loaded.")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(AD_LOG_TAG, "Banner ad failed: code=${error.code}, msg=${error.message}")
                        // 常见: 0=内部错误 1=请求无效 2=网络错误 3=无填充(无广告可展示)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
