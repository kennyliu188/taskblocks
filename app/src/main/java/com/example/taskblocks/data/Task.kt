package com.example.taskblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    /** 重要性 1-4 */
    val importance: Int,
    /** 分类 id，对应 categories 表 */
    val categoryId: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    /** 完成时间，用于看板时间轴 */
    val completedAt: Long? = null,
    /** 截止日期（当天 0 点时间戳），null 表示不设截止日 */
    val deadlineAt: Long? = null,
    /** 完成评分 1-3：一般、好、非常好，null 表示未评分（旧数据兼容） */
    val completionStars: Int? = null,
    /** 方块是否旋转放置（用于支持横/竖堆叠） */
    val rotated: Boolean = false,
    /** 在网格中的左上角列 (0-based) */
    val gridCol: Int = 0,
    /** 在网格中的左上角行 (0-based)，0 为顶部 */
    val gridRow: Int = 0
) {
    fun importanceLevel(): ImportanceLevel = ImportanceLevel.fromLevel(importance)
}
