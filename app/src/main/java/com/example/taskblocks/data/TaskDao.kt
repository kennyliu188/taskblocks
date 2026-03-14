package com.example.taskblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY gridRow DESC, gridCol ASC")
    fun getAllActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: Long)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt, completionStars = :stars WHERE id = :id")
    suspend fun markCompletedWithStars(id: Long, completedAt: Long, stars: Int)

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getActiveTasksSync(): List<Task>

    /** 更新任务的网格位置（重力后重算） */
    @Query("UPDATE tasks SET gridRow = :row, gridCol = :col WHERE id = :id")
    suspend fun updatePosition(id: Long, row: Int, col: Int)

    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId AND isCompleted = 0")
    suspend fun countTasksByCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun countActive(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun countCompleted(): Int

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllSync(): List<Task>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
}
