package com.example.taskblocks.data

import androidx.annotation.StringRes
import com.example.taskblocks.R

/**
 * 任务重要性等级（1-4），对应方块的占用格数。
 * 等级越高，方块越大。
 *
 * 语义上对应四象限：
 * 1 - 不重要不紧急
 * 2 - 不重要但紧急
 * 3 - 重要但不紧急
 * 4 - 重要而且紧急
 */
enum class ImportanceLevel(val level: Int, val gridWidth: Int, val gridHeight: Int, @StringRes val displayNameResId: Int) {
    LOW(1, 1, 1, R.string.importance_low),
    MEDIUM(2, 2, 1, R.string.importance_medium),
    HIGH(3, 2, 2, R.string.importance_high),
    URGENT(4, 3, 2, R.string.importance_urgent);

    companion object {
        fun fromLevel(level: Int): ImportanceLevel = entries.find { it.level == level } ?: LOW
    }
}
