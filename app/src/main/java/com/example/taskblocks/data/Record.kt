package com.example.taskblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 附件：type 为 "image" 或 "document"，path 为应用内部存储路径 */
data class RecordAttachment(val type: String, val path: String)

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val content: String = "",
    /** JSON 数组，如 [{"type":"image","path":"..."}] */
    val attachmentsJson: String = "[]"
) {
    fun parseAttachments(): List<RecordAttachment> {
        if (attachmentsJson.isBlank() || attachmentsJson == "[]") return emptyList()
        return try {
            org.json.JSONArray(attachmentsJson).let { arr ->
                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    RecordAttachment(
                        type = obj.optString("type", "image"),
                        path = obj.optString("path", "")
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun hasImage(): Boolean = parseAttachments().any { it.type == "image" }
    fun hasDocument(): Boolean = parseAttachments().any { it.type == "document" }
}
