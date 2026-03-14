package com.example.taskblocks.ui

import android.app.Application
import android.net.Uri
import com.example.taskblocks.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskblocks.data.Category
import com.example.taskblocks.data.RecordAttachment
import com.example.taskblocks.data.DatabaseProvider
import com.example.taskblocks.data.Record
import com.example.taskblocks.data.BlockStyle
import com.example.taskblocks.data.Task
import com.example.taskblocks.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.get(application)
    private val repository = TaskRepository(db.taskDao(), db.categoryDao(), db.recordDao())

    val activeTasks: StateFlow<List<Task>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _gridFullMessage = MutableStateFlow<String?>(null)
    val gridFullMessage: StateFlow<String?> = _gridFullMessage.asStateFlow()

    private val prefs = application.getSharedPreferences("taskblocks_prefs", android.content.Context.MODE_PRIVATE)
    private val _blockStyle = MutableStateFlow(
        BlockStyle.fromId(prefs.getString("block_style", null))
    )
    val blockStyle: StateFlow<BlockStyle> = _blockStyle.asStateFlow()

    fun setBlockStyle(style: BlockStyle) {
        _blockStyle.value = style
        prefs.edit().putString("block_style", style.id).apply()
    }

    fun clearGridFullMessage() { _gridFullMessage.value = null }

    fun addTask(
        title: String,
        description: String,
        importance: Int,
        categoryId: Long,
        deadlineAt: Long? = null
    ) {
        viewModelScope.launch {
            repository.addTask(title, description, importance, categoryId, deadlineAt)
                .onFailure {
                    if (it is IllegalStateException && it.message == "GRID_FULL") {
                        _gridFullMessage.value = getApplication<Application>().getString(R.string.grid_full_message)
                    }
                }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun completeTask(id: Long) {
        viewModelScope.launch { repository.completeTask(id) }
    }

    /** 消失动画：保存正在动画的任务及动作（取消=删除 / 完成=记录看板） */
    private val _completingTask = MutableStateFlow<Task?>(null)
    val completingTask: StateFlow<Task?> = _completingTask.asStateFlow()
    private var completingAction: Boolean = true // true=完成记录看板, false=取消不记录
    private var completingStars: Int? = null // 完成评分 1-3

    /** 待评分的任务：点击完成后先弹出评分，选星后调用 startDisappearAnimation */
    private val _taskForRating = MutableStateFlow<Task?>(null)
    val taskForRating: StateFlow<Task?> = _taskForRating.asStateFlow()

    fun showCompletionRating(task: Task) {
        _taskForRating.value = task
    }

    fun dismissCompletionRating() {
        _taskForRating.value = null
    }

    fun startDisappearAnimation(task: Task, isComplete: Boolean, stars: Int? = null) {
        completingAction = isComplete
        completingStars = stars
        _completingTask.value = task
        _taskForRating.value = null
    }

    fun finishDisappearAnimation() {
        viewModelScope.launch {
            val task = _completingTask.value ?: return@launch
            if (completingAction) repository.completeTask(task.id, completingStars)
            else repository.deleteTask(task.id)
            _completingTask.value = null
            completingStars = null
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repository.deleteTask(id) }
    }

    /** 清空所有数据（含附件），恢复默认分类 */
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val names = listOf(
                app.getString(R.string.category_work),
                app.getString(R.string.category_life),
                app.getString(R.string.category_study),
                app.getString(R.string.category_other)
            )
            repository.clearAllData(recordsDir(), names)
        }
    }

    /** 删除已选中的已完成任务及附件 */
    fun deleteCompletedTasks(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCompletedTasks(ids, recordsDir())
        }
    }

    suspend fun canAddTask(importance: Int): Boolean {
        val level = com.example.taskblocks.data.ImportanceLevel.fromLevel(importance)
        return repository.canPlaceBlock(level.gridWidth, level.gridHeight)
    }

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    fun loadTask(id: Long) {
        viewModelScope.launch {
            _selectedTask.value = repository.getTaskById(id)
        }
    }

    fun clearSelectedTask() { _selectedTask.value = null }

    private val _detailTaskId = MutableStateFlow<Long?>(null)
    private val _taskRecords = MutableStateFlow<List<Record>>(emptyList())
    val taskRecords: StateFlow<List<Record>> = _taskRecords.asStateFlow()

    init {
        viewModelScope.launch {
            _detailTaskId.flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else repository.getRecordsForTask(id)
            }.collect { _taskRecords.value = it }
        }
    }

    fun setDetailTaskId(taskId: Long?) { _detailTaskId.value = taskId }

    suspend fun getRecordById(id: Long): Record? = repository.getRecordById(id)

    fun addRecord(taskId: Long, content: String, attachments: List<RecordAttachment>) {
        viewModelScope.launch {
            val json = attachmentsToJson(attachments)
            repository.insertRecord(Record(taskId = taskId, content = content, attachmentsJson = json))
        }
    }

    /** 从选中的图片/文档 Uri 保存记录（先插入再复制文件再更新） */
    fun addRecordWithUris(taskId: Long, content: String, imageUris: List<Uri>, documentUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = Record(taskId = taskId, content = content, attachmentsJson = "[]")
            val id = repository.insertRecord(record)
            val app = getApplication<Application>()
            val dir = File(app.filesDir, "records").also { it.mkdirs() }
            val list = mutableListOf<RecordAttachment>()
            imageUris.forEachIndexed { i, uri ->
                val ext = "jpg"
                val f = File(dir, "${id}_img_$i.$ext")
                copyUriToFile(app, uri, f)
                list.add(RecordAttachment("image", f.absolutePath))
            }
            documentUris.forEachIndexed { i, uri ->
                val name = uri.lastPathSegment ?: "doc_$i"
                val ext = name.substringAfterLast('.', "bin")
                val f = File(dir, "${id}_doc_$i.$ext")
                copyUriToFile(app, uri, f)
                list.add(RecordAttachment("document", f.absolutePath))
            }
            repository.updateRecord(record.copy(id = id, attachmentsJson = attachmentsToJson(list)))
        }
    }

    private fun attachmentsToJson(attachments: List<RecordAttachment>): String {
        if (attachments.isEmpty()) return "[]"
        return buildString {
            append("[")
            attachments.forEachIndexed { i, a ->
                if (i > 0) append(",")
                append("{\"type\":\"${a.type}\",\"path\":\"${a.path.replace("\\", "\\\\").replace("\"", "\\\"")}\"}")
            }
            append("]")
        }
    }

    private fun copyUriToFile(app: Application, uri: Uri, dest: File) {
        app.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { input.copyTo(it) }
        }
    }

    fun addCategory(name: String, colorInt: Int) {
        viewModelScope.launch { repository.addCategory(name, colorInt) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repository.updateCategory(category) }
    }

    fun deleteCategory(id: Long, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            if (repository.deleteCategory(id)) onSuccess() else onFailure()
        }
    }

    suspend fun getTaskCounts(): Triple<Int, Int, Int> = repository.getTaskCounts()

    suspend fun exportToJson(): String = repository.exportToJson()

    suspend fun importFromJson(json: String): Result<Unit> = repository.importFromJson(json)

    /**  records 目录，用于备份/导入附件 */
    private fun recordsDir(): File =
        File(getApplication<Application>().filesDir, "records").also { it.mkdirs() }

    /** 导出为 ZIP（含图片和文档附件） */
    suspend fun exportToZip(outputStream: OutputStream) =
        repository.exportToZip(outputStream, recordsDir())

    /** 从 ZIP 导入（还原附件） */
    suspend fun importFromZip(inputStream: InputStream): Result<Unit> =
        repository.importFromZip(inputStream, recordsDir())

    /** 根据内容自动检测 ZIP 或 JSON，执行对应导入 */
    suspend fun importFromStream(inputStream: InputStream): Result<Unit> {
        val bytes = inputStream.readBytes()
        return if (bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
            importFromZip(java.io.ByteArrayInputStream(bytes))
        } else {
            importFromJson(String(bytes, Charsets.UTF_8))
        }
    }
}
