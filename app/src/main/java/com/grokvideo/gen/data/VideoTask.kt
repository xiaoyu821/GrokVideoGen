package com.grokvideo.gen.data

import java.util.Date

data class VideoTask(
    val id: String,
    val taskId: String,
    val prompt: String,
    val model: String,
    val status: TaskStatus,
    val progress: Int = 0,
    val videoUrl: String? = null,
    val duration: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED
}
