package com.grokvideo.gen.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class HistoryManager(private val context: Context) {
    private val gson = Gson()
    private val historyFile = File(context.filesDir, "video_history.json")
    
    fun loadHistory(): List<VideoTask> {
        return try {
            if (historyFile.exists()) {
                val json = historyFile.readText()
                val type = object : TypeToken<List<VideoTask>>() {}.type
                val tasks: List<VideoTask> = gson.fromJson(json, type)
                tasks.sortedByDescending { it.createdAt }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun addTask(task: VideoTask) {
        val current = loadHistory().toMutableList()
        current.add(0, task)
        saveToFile(current)
    }
    
    fun updateTask(task: VideoTask) {
        val current = loadHistory().toMutableList()
        val index = current.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            current[index] = task
            saveToFile(current)
        }
    }
    
    fun deleteTask(taskId: String) {
        val current = loadHistory().filter { it.id != taskId }
        saveToFile(current)
    }
    
    private fun saveToFile(tasks: List<VideoTask>) {
        try {
            val json = gson.toJson(tasks)
            historyFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
