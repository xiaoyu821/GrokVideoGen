package com.grokvideo.gen.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    suspend fun createVideoTask(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String
    ): CreateTaskResponse = withContext(Dispatchers.IO) {
        val url = "$baseUrl/v1/videos/generations"
        
        val json = JsonObject().apply {
            addProperty("model", model)
            addProperty("prompt", prompt)
        }
        
        val body = json.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }
        
        gson.fromJson(responseBody, CreateTaskResponse::class.java)
    }
    
    suspend fun checkTaskStatus(
        baseUrl: String,
        apiKey: String,
        taskId: String
    ): TaskStatusResponse = withContext(Dispatchers.IO) {
        val url = "$baseUrl/v1/videos/$taskId"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }
        
        gson.fromJson(responseBody, TaskStatusResponse::class.java)
    }
}

data class CreateTaskResponse(
    val request_id: String?,
    val id: String?,
    val task_id: String?,
    val status: String?
)

data class TaskStatusResponse(
    val id: String,
    val model: String?,
    val status: String,
    val progress: Int,
    val video: VideoInfo?
)

data class VideoInfo(
    val url: String,
    val duration: Int?
)
