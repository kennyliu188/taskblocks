package com.example.taskblocks.data

import androidx.compose.ui.graphics.Color

/**
 * 任务分类，不同分类对应不同颜色的方块。
 */
enum class TaskCategory(
    val displayName: String,
    val color: Color
) {
    WORK("工作", Color(0xFF4CAF50)),
    LIFE("生活", Color(0xFF2196F3)),
    STUDY("学习", Color(0xFFFF9800)),
    OTHER("其他", Color(0xFF9C27B0));

    companion object {
        fun fromOrdinal(ordinal: Int): TaskCategory =
            entries.getOrElse(ordinal) { OTHER }
    }
}
