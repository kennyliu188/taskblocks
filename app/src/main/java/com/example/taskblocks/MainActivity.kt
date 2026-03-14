package com.example.taskblocks

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskblocks.ui.AddEditTaskScreen
import com.example.taskblocks.ui.SplashScreen
import com.example.taskblocks.ui.HomeScreen
import com.example.taskblocks.ui.KanbanScreen
import com.example.taskblocks.ui.DataManagementScreen
import com.example.taskblocks.ui.SettingsOverviewScreen
import com.example.taskblocks.ui.SettingsScreen
import com.example.taskblocks.ui.BlockStyleSelectionScreen
import com.example.taskblocks.ui.RecordDetailScreen
import com.example.taskblocks.ui.RecordEditScreen
import com.example.taskblocks.ui.TaskDetailScreen
import com.example.taskblocks.ui.TaskViewModel
import com.example.taskblocks.ui.TaskViewModelFactory
import com.example.taskblocks.ui.theme.TaskblocksTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            TaskblocksTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TaskblocksApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TaskblocksApp(
    viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val selectedTask by viewModel.selectedTask.collectAsState()
    val categories by viewModel.categories.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onTaskClick = { id -> navController.navigate("detail/$id") },
                onAddClick = { navController.navigate("add") },
                onKanbanClick = { navController.navigate("kanban") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("kanban") {
            KanbanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTaskClick = { id -> navController.navigate("detail/$id") }
            )
        }
        composable(
            route = "detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            TaskDetailScreen(
                taskId = taskId,
                viewModel = viewModel,
                categories = categories,
                onEdit = { id -> navController.navigate("edit/$id") },
                onBack = {
                    viewModel.clearSelectedTask()
                    viewModel.setDetailTaskId(null)
                    navController.popBackStack()
                },
                onCompleteAndBack = {
                    viewModel.setDetailTaskId(null)
                    navController.popBackStack()
                },
                onRecordClick = { tid -> navController.navigate("task/$tid/record/add") },
                onRecordItemClick = { recordId -> navController.navigate("record/$recordId") }
            )
        }
        composable(
            route = "task/{taskId}/record/add",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            RecordEditScreen(
                taskId = taskId,
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "record/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
            RecordDetailScreen(
                recordId = recordId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("add") {
            AddEditTaskScreen(
                existingTask = null,
                categories = categories,
                onSave = { title, description, importance, categoryId, deadlineAt ->
                    viewModel.addTask(title, description, importance, categoryId, deadlineAt)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            LaunchedEffect(taskId) { viewModel.loadTask(taskId) }
            val task by viewModel.selectedTask.collectAsState()
            val currentTask = task
            if (currentTask == null) {
                androidx.compose.material3.Text(stringResource(R.string.loading), modifier = Modifier.padding(16.dp))
            } else {
                AddEditTaskScreen(
                    existingTask = currentTask,
                    categories = categories,
                    onSave = { title, description, importance, categoryId, deadlineAt ->
                        viewModel.updateTask(
                            currentTask.copy(
                                title = title,
                                description = description,
                                importance = importance,
                                categoryId = categoryId,
                                deadlineAt = deadlineAt
                            )
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("settings") {
            SettingsOverviewScreen(
                onBack = { navController.popBackStack() },
                onCategoryManagement = { navController.navigate("settings/categories") },
                onDataManagement = { navController.navigate("settings/data") },
                onBlockStyle = { navController.navigate("settings/blockStyle") }
            )
        }
        composable("settings/blockStyle") {
            val blockStyle by viewModel.blockStyle.collectAsState()
            BlockStyleSelectionScreen(
                currentStyle = blockStyle,
                onStyleSelected = { viewModel.setBlockStyle(it); navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings/data") {
            DataManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings/categories") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}