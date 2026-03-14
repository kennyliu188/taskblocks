package com.example.taskblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Query("SELECT * FROM records WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getByTaskId(taskId: Long): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): Record?

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Long)

    @Query("SELECT * FROM records WHERE taskId = :taskId")
    suspend fun getByTaskIdSync(taskId: Long): List<Record>

    @Query("SELECT * FROM records ORDER BY id ASC")
    suspend fun getAllSync(): List<Record>

    @Query("DELETE FROM records")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<Record>)
}
