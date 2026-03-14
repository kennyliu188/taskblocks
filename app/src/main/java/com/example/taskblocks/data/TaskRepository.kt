package com.example.taskblocks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class TaskRepository(
    private val dao: TaskDao,
    private val categoryDao: CategoryDao,
    private val recordDao: RecordDao
) {

    val activeTasks: Flow<List<Task>> = dao.getAllActiveTasks()
    val categories: Flow<List<Category>> = categoryDao.getAll()

    suspend fun getTaskById(id: Long): Task? = dao.getTaskById(id)
    suspend fun getCategoriesSync(): List<Category> = categoryDao.getAllSync()

    /** 检查网格是否有足够空位放置指定大小的方块（支持旋转，优先考虑更靠下的空间） */
    suspend fun canPlaceBlock(width: Int, height: Int): Boolean {
        val occupied = buildOccupiedSet(dao.getActiveTasksSync())
        val sizes = buildList {
            add(width to height)
            if (width != height) add(height to width)
        }.distinct()
        for ((w, h) in sizes) {
            val maxRow = (GRID_ROWS - h).coerceAtLeast(0)
            for (row in maxRow downTo 0) {
                for (col in 0..(GRID_COLS - w)) {
                    if (fits(occupied, row, col, w, h, null)) return true
                }
            }
        }
        return false
    }

    /** 添加任务并自动分配网格位置 */
    suspend fun addTask(
        title: String,
        description: String,
        importance: Int,
        categoryId: Long,
        deadlineAt: Long? = null
    ): Result<Long> {
        val level = ImportanceLevel.fromLevel(importance)
        if (!canPlaceBlock(level.gridWidth, level.gridHeight)) {
            return Result.failure(IllegalStateException("GRID_FULL"))
        }
        val placement = findPlacement(level.gridWidth, level.gridHeight)!!
        val task = Task(
            title = title,
            description = description,
            importance = importance,
            categoryId = categoryId,
            deadlineAt = deadlineAt,
            gridRow = placement.row,
            gridCol = placement.col,
            rotated = placement.rotated
        )
        val id = dao.insert(task)
        return Result.success(id)
    }

    suspend fun updateTask(task: Task) {
        dao.update(task)
    }

    /** 标记完成并执行重力：上方方块下落。stars 为 1-3 时写入评分 */
    suspend fun completeTask(id: Long, stars: Int? = null) {
        val task = dao.getTaskById(id) ?: return
        val now = System.currentTimeMillis()
        if (stars != null && stars in 1..3) {
            dao.markCompletedWithStars(id, now, stars)
        } else {
            dao.markCompleted(id, now)
        }
        applyGravity(excludeId = id)
    }

    val completedTasks: Flow<List<Task>> = dao.getCompletedTasks()

    suspend fun deleteTask(id: Long) {
        val task = dao.getTaskById(id) ?: return
        deleteTaskWithAttachments(id, null)
        if (!task.isCompleted) applyGravity(excludeId = id)
    }

    /** 清空所有数据（任务、记录、附件、分类），并恢复默认分类 */
    suspend fun clearAllData(recordsDir: File?, defaultCategoryNames: List<String>) {
        val records = recordDao.getAllSync()
        records.forEach { record ->
            record.parseAttachments().forEach { att ->
                val f = File(att.path)
                if (f.exists()) f.delete()
            }
        }
        recordDao.deleteAll()
        dao.deleteAll()
        categoryDao.deleteAll()
        defaultCategoryNames.forEachIndexed { index, name ->
            val colorInt = when (index) {
                0 -> 0xFF4CAF50.toInt()
                1 -> 0xFF2196F3.toInt()
                2 -> 0xFFFF9800.toInt()
                else -> 0xFF9C27B0.toInt()
            }
            categoryDao.insert(Category(name = name, colorInt = colorInt, sortOrder = index))
        }
    }

    /** 删除已完成任务及其记录、附件（recordsDir 用于删除附件文件，可为 null） */
    suspend fun deleteCompletedTasks(ids: List<Long>, recordsDir: File?) {
        ids.forEach { id ->
            deleteTaskWithAttachments(id, recordsDir)
        }
    }

    private suspend fun deleteTaskWithAttachments(id: Long, recordsDir: File?) {
        val records = recordDao.getByTaskIdSync(id)
        records.forEach { record ->
            record.parseAttachments().forEach { att ->
                val f = File(att.path)
                if (f.exists()) f.delete()
            }
        }
        recordDao.deleteByTaskId(id)
        dao.delete(id)
    }

    suspend fun addCategory(name: String, colorInt: Int) {
        val maxOrder = categoryDao.maxSortOrder() ?: -1
        categoryDao.insert(Category(name = name, colorInt = colorInt, sortOrder = maxOrder + 1))
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(id: Long): Boolean {
        if (dao.countTasksByCategory(id) > 0) return false
        categoryDao.delete(id)
        return true
    }

    fun getRecordsForTask(taskId: Long): Flow<List<Record>> = recordDao.getByTaskId(taskId)

    suspend fun getTaskCounts(): Triple<Int, Int, Int> {
        val total = dao.countAll()
        val active = dao.countActive()
        val completed = dao.countCompleted()
        return Triple(total, active, completed)
    }

    suspend fun exportToJson(): String {
        val records = recordDao.getAllSync()
        val categories = categoryDao.getAllSync()
        val tasks = dao.getAllSync()
        return buildExportJson(categories, tasks, records).toString(2)
    }

    /** 导出为 ZIP，包含 data.json 和 attachments 文件夹中的图片/文档 */
    suspend fun exportToZip(outputStream: OutputStream, recordsDir: File) {
        val records = recordDao.getAllSync()
        val categories = categoryDao.getAllSync()
        val tasks = dao.getAllSync()
        val (exportJson, attachmentEntries) = buildExportJsonWithAttachments(records, recordsDir, categories, tasks)
        ZipOutputStream(outputStream).use { zos ->
            zos.putNextEntry(ZipEntry("data.json"))
            zos.write(exportJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            attachmentEntries.forEach { (zipPath, srcFile) ->
                if (srcFile.exists()) {
                    zos.putNextEntry(ZipEntry(zipPath))
                    srcFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }

    private fun buildExportJson(categories: List<Category>, tasks: List<Task>, records: List<Record>): JSONObject =
        jsonRoot(categories, tasks, records)

    private fun buildExportJsonWithAttachments(
        records: List<Record>,
        recordsDir: File,
        categories: List<Category>,
        tasks: List<Task>
    ): Pair<JSONObject, List<Pair<String, File>>> {
        val attachmentList = mutableListOf<Pair<String, File>>()
        val recordsArr = JSONArray()
        records.forEach { r ->
            val newAttachments = mutableListOf<Pair<String, String>>()
            r.parseAttachments().forEachIndexed { i, att ->
                val srcFile = File(att.path)
                if (srcFile.exists()) {
                    val name = "${r.id}_${i}_${srcFile.name}"
                    newAttachments.add("attachments/$name" to att.type)
                    attachmentList.add("attachments/$name" to srcFile)
                }
            }
            val newJson = if (newAttachments.isEmpty()) "[]" else
                newAttachments.joinToString(",") { "{\"type\":\"${it.second}\",\"path\":\"${it.first}\"}" }.let { "[$it]" }
            recordsArr.put(JSONObject().apply {
                put("id", r.id)
                put("taskId", r.taskId)
                put("content", r.content)
                put("attachmentsJson", newJson)
                put("createdAt", r.createdAt)
            })
        }
        val root = JSONObject().apply {
            put("format", "taskblocks")
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("categories", categoryArray(categories))
            put("tasks", taskArray(tasks))
            put("records", recordsArr)
        }
        return root to attachmentList
    }

    private fun jsonRoot(categories: List<Category>, tasks: List<Task>, records: List<Record>): JSONObject {
        return JSONObject().apply {
            put("format", "taskblocks")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("categories", categoryArray(categories))
            put("tasks", taskArray(tasks))
            put("records", JSONArray().apply {
                records.forEach { r ->
                    put(JSONObject().apply {
                        put("id", r.id)
                        put("taskId", r.taskId)
                        put("content", r.content)
                        put("attachmentsJson", r.attachmentsJson)
                        put("createdAt", r.createdAt)
                    })
                }
            })
        }
    }

    private fun categoryArray(categories: List<Category>) = JSONArray().apply {
        categories.forEach { c ->
            put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("colorInt", c.colorInt)
                put("sortOrder", c.sortOrder)
            })
        }
    }

    private fun taskArray(tasks: List<Task>) = JSONArray().apply {
        tasks.forEach { t ->
            put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("importance", t.importance)
                put("categoryId", t.categoryId)
                put("isCompleted", t.isCompleted)
                put("createdAt", t.createdAt)
                put("completedAt", t.completedAt ?: JSONObject.NULL)
                put("rotated", t.rotated)
                put("gridCol", t.gridCol)
                put("gridRow", t.gridRow)
                put("completionStars", t.completionStars ?: JSONObject.NULL)
                put("deadlineAt", t.deadlineAt ?: JSONObject.NULL)
            })
        }
    }

    suspend fun importFromJson(json: String): Result<Unit> = runCatching {
        val root = JSONObject(json)
        if (root.optString("format") != "taskblocks") {
            throw IllegalArgumentException("FORMAT_ERROR")
        }
        val ver = root.optInt("version", 0)
        if (ver != 1 && ver != 2) {
            throw IllegalArgumentException("FORMAT_ERROR")
        }
        recordDao.deleteAll()
        dao.deleteAll()
        categoryDao.deleteAll()

        val catArr = root.getJSONArray("categories")
        val categories = mutableListOf<Category>()
        for (i in 0 until catArr.length()) {
            val o = catArr.getJSONObject(i)
            categories.add(Category(
                id = o.getLong("id"),
                name = o.getString("name"),
                colorInt = o.getInt("colorInt"),
                sortOrder = o.optInt("sortOrder", 0)
            ))
        }
        categoryDao.insertAll(categories)

        val taskArr = root.getJSONArray("tasks")
        val tasks = mutableListOf<Task>()
        for (i in 0 until taskArr.length()) {
            val o = taskArr.getJSONObject(i)
            val stars = if (o.has("completionStars") && !o.isNull("completionStars")) {
                o.optInt("completionStars", -1).takeIf { it in 1..3 }
            } else null
            tasks.add(Task(
                id = o.getLong("id"),
                title = o.getString("title"),
                description = o.optString("description", ""),
                importance = o.getInt("importance"),
                categoryId = o.getLong("categoryId"),
                isCompleted = o.optBoolean("isCompleted", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                completedAt = if (o.isNull("completedAt")) null else o.getLong("completedAt"),
                completionStars = stars,
                deadlineAt = if (o.has("deadlineAt") && !o.isNull("deadlineAt")) o.getLong("deadlineAt") else null,
                rotated = o.optBoolean("rotated", false),
                gridCol = o.optInt("gridCol", 0),
                gridRow = o.optInt("gridRow", 0)
            ))
        }
        dao.insertAll(tasks)

        val recArr = root.getJSONArray("records")
        val records = mutableListOf<Record>()
        for (i in 0 until recArr.length()) {
            val o = recArr.getJSONObject(i)
            records.add(Record(
                id = o.getLong("id"),
                taskId = o.getLong("taskId"),
                content = o.optString("content", ""),
                attachmentsJson = o.optString("attachmentsJson", "[]"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            ))
        }
        recordDao.insertAll(records)
    }

    /** 从 ZIP 导入：解压 data.json 和 attachments，将附件复制到 recordsDir，再导入数据 */
    suspend fun importFromZip(inputStream: InputStream, recordsDir: File): Result<Unit> = runCatching {
            val jsonBytes = mutableListOf<Byte>()
            val attachmentPaths = mutableListOf<Pair<String, ByteArray>>()
            ZipInputStream(inputStream).use { zis ->
                var e = zis.nextEntry
                while (e != null) {
                    when {
                        e.name == "data.json" -> jsonBytes.addAll(zis.readBytes().toList())
                        e.name.startsWith("attachments/") && !e.isDirectory -> {
                            val name = e.name.removePrefix("attachments/")
                            attachmentPaths.add(name to zis.readBytes())
                        }
                    }
                    zis.closeEntry()
                    e = zis.nextEntry
                }
            }
            val jsonStr = String(jsonBytes.toByteArray(), Charsets.UTF_8)
            val root = JSONObject(jsonStr)
            if (root.optString("format") != "taskblocks") throw IllegalArgumentException("FORMAT_ERROR")
            val ver = root.optInt("version", 0)
            if (ver != 1 && ver != 2) throw IllegalArgumentException("FORMAT_ERROR")

            val pathMap = mutableMapOf<String, String>()
            attachmentPaths.forEach { (name, bytes) ->
                val dest = File(recordsDir, name)
                dest.writeBytes(bytes)
                pathMap["attachments/$name"] = dest.absolutePath
            }

            val recArr = root.getJSONArray("records")
            for (i in 0 until recArr.length()) {
                val o = recArr.getJSONObject(i)
                var attJson = o.optString("attachmentsJson", "[]")
                pathMap.forEach { (oldPath, newPath) ->
                    attJson = attJson.replace("\"$oldPath\"", "\"$newPath\"")
                }
                o.put("attachmentsJson", attJson)
            }

            importFromJson(root.toString()).getOrThrow()
    }

    suspend fun getRecordById(id: Long): Record? = recordDao.getById(id)
    suspend fun insertRecord(record: Record): Long = recordDao.insert(record)
    suspend fun updateRecord(record: Record) { recordDao.update(record) }
    suspend fun deleteRecord(id: Long) { recordDao.delete(id) }

    private data class Placement(val row: Int, val col: Int, val rotated: Boolean)

    /** 找出可放置的 (row, col, rotated)：支持横/竖两种堆叠（旋转），优先更靠下的空间 */
    private suspend fun findPlacement(width: Int, height: Int): Placement? {
        val occupied = buildOccupiedSet(dao.getActiveTasksSync())
        val candidates = buildList {
            add(Triple(width, height, false))
            if (width != height) add(Triple(height, width, true))
        }

        var best: Placement? = null
        for ((w, h, rotated) in candidates) {
            val maxRow = (GRID_ROWS - h).coerceAtLeast(0)
            var found: Placement? = null
            loop@ for (row in maxRow downTo 0) {
                for (col in 0..(GRID_COLS - w)) {
                    if (fits(occupied, row, col, w, h, null)) {
                        found = Placement(row, col, rotated)
                        break@loop
                    }
                }
            }
            if (found != null) {
                best = when {
                    best == null -> found
                    found.row > best!!.row -> found
                    found.row < best!!.row -> best
                    found.col < best!!.col -> found
                    found.col > best!!.col -> best
                    // 同位置优先不旋转
                    best!!.rotated && !found.rotated -> found
                    else -> best
                }
            }
        }
        return best
    }

    private fun buildOccupiedSet(tasks: List<Task>): Set<Pair<Int, Int>> {
        val set = mutableSetOf<Pair<Int, Int>>()
        tasks.forEach { task ->
            val (w, h) = taskSize(task)
            for (r in task.gridRow until task.gridRow + h) {
                for (c in task.gridCol until task.gridCol + w) {
                    set.add(r to c)
                }
            }
        }
        return set
    }

    private fun taskSize(task: Task): Pair<Int, Int> {
        val level = task.importanceLevel()
        val w = level.gridWidth
        val h = level.gridHeight
        return if (task.rotated) h to w else w to h
    }

    private fun fits(
        occupied: Set<Pair<Int, Int>>,
        row: Int,
        col: Int,
        width: Int,
        height: Int,
        excludeId: Long?
    ): Boolean {
        for (r in row until row + height) {
            for (c in col until col + width) {
                if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false
                if (occupied.contains(r to c)) return false
            }
        }
        return true
    }

    /** 完成/删除后：上方方块下落，按行从顶到底重算位置 */
    private suspend fun applyGravity(excludeId: Long) {
        val tasks = dao.getActiveTasksSync().filter { it.id != excludeId }
        if (tasks.isEmpty()) return
        val sortedByRow = tasks.sortedBy { it.gridRow }
        val used = mutableSetOf<Pair<Int, Int>>()

        for (task in sortedByRow) {
            val (w, h) = taskSize(task)
            val col = task.gridCol
            val surface = (col until col + w).minOfOrNull { c ->
                (0 until GRID_ROWS).firstOrNull { r -> used.contains(r to c) } ?: GRID_ROWS
            } ?: GRID_ROWS
            val newRow = (surface - h).coerceAtLeast(0)
            if (newRow != task.gridRow) {
                dao.updatePosition(task.id, newRow, col)
            }
            for (r in newRow until newRow + h) {
                for (c in col until col + w) {
                    used.add(r to c)
                }
            }
        }
    }
}
