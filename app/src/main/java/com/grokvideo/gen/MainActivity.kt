package com.grokvideo.gen

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.grokvideo.gen.data.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var promptInput: EditText
    private lateinit var generateButton: Button
    private lateinit var progressLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var errorCard: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var resultCard: LinearLayout
    private lateinit var videoThumbnail: ImageView
    private lateinit var playButton: Button
    private lateinit var downloadButton: Button
    
    private lateinit var apiService: ApiService
    private lateinit var prefsManager: PreferencesManager
    private lateinit var historyManager: HistoryManager
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentVideoUrl: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 莫奈动态取色
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        apiService = ApiService()
        prefsManager = PreferencesManager(this)
        historyManager = HistoryManager(this)
        
        initViews()
        setupListeners()
    }
    
    private fun initViews() {
        promptInput = findViewById(R.id.promptInput)
        generateButton = findViewById(R.id.generateButton)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        errorCard = findViewById(R.id.errorCard)
        errorText = findViewById(R.id.errorText)
        resultCard = findViewById(R.id.resultCard)
        videoThumbnail = findViewById(R.id.videoThumbnail)
        playButton = findViewById(R.id.playButton)
        downloadButton = findViewById(R.id.downloadButton)
        
        // 顶部按钮
        findViewById<ImageButton>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun setupListeners() {
        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isBlank()) {
                showError("请输入视频描述")
                return@setOnClickListener
            }
            generateVideo(prompt)
        }
        
        playButton.setOnClickListener {
            currentVideoUrl?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        
        downloadButton.setOnClickListener {
            currentVideoUrl?.let { url ->
                try {
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setTitle("Grok 视频")
                    request.setDescription("正在下载视频...")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GrokVideo_${System.currentTimeMillis()}.mp4")
                    request.setMimeType("video/mp4")
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
                    Toast.makeText(this, "⬇️ 开始下载到 Download 文件夹", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun generateVideo(prompt: String) {
        val apiKey = prefsManager.apiKey
        if (apiKey.isBlank()) {
            showError("请先在设置中配置 API Key")
            return
        }
        
        hideError()
        hideResult()
        showProgress()
        generateButton.isEnabled = false
        generateButton.alpha = 0.5f
        
        scope.launch {
            try {
                val baseUrl = prefsManager.baseUrl
                val model = prefsManager.model
                
                val createResponse = withContext(Dispatchers.IO) {
                    apiService.createVideoTask(baseUrl, apiKey, model, prompt)
                }
                
                val taskId = createResponse.task_id ?: createResponse.id
                    ?: throw Exception("未获取到任务 ID")
                
                val task = VideoTask(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    prompt = prompt,
                    model = model,
                    status = TaskStatus.PENDING
                )
                
                withContext(Dispatchers.IO) { historyManager.addTask(task) }
                pollTaskStatus(baseUrl, apiKey, taskId, task)
                
            } catch (e: Exception) {
                hideProgress()
                generateButton.isEnabled = true
                generateButton.alpha = 1f
                showError(e.message ?: "未知错误")
            }
        }
    }
    
    private suspend fun pollTaskStatus(baseUrl: String, apiKey: String, taskId: String, task: VideoTask) {
        var attempts = 0
        while (attempts < 60) {
            delay(5000)
            attempts++
            try {
                val sr = withContext(Dispatchers.IO) {
                    apiService.checkTaskStatus(baseUrl, apiKey, taskId)
                }
                updateProgress(sr.progress)
                
                withContext(Dispatchers.IO) {
                    historyManager.updateTask(task.copy(
                        status = when (sr.status) { "done" -> TaskStatus.DONE; "failed" -> TaskStatus.FAILED; else -> TaskStatus.PROCESSING },
                        progress = sr.progress,
                        videoUrl = sr.video?.url,
                        duration = sr.video?.duration,
                        completedAt = if (sr.status == "done") System.currentTimeMillis() else null
                    ))
                }
                
                when (sr.status) {
                    "done" -> {
                        hideProgress()
                        generateButton.isEnabled = true
                        generateButton.alpha = 1f
                        showResult(sr.video?.url)
                        return
                    }
                    "failed" -> throw Exception("视频生成失败")
                }
            } catch (e: Exception) {
                hideProgress()
                generateButton.isEnabled = true
                generateButton.alpha = 1f
                showError(e.message ?: "未知错误")
                return
            }
        }
        hideProgress()
        generateButton.isEnabled = true
        generateButton.alpha = 1f
        showError("生成超时")
    }
    
    private fun showProgress() { progressLayout.visibility = View.VISIBLE; progressBar.progress = 0; progressText.text = "0%" }
    private fun hideProgress() { progressLayout.visibility = View.GONE }
    private fun updateProgress(p: Int) { progressBar.progress = p; progressText.text = "$p%" }
    private fun showError(msg: String) { errorCard.visibility = View.VISIBLE; errorText.text = msg }
    private fun hideError() { errorCard.visibility = View.GONE }
    private fun showResult(url: String?) { if (url == null) return; currentVideoUrl = url; resultCard.visibility = View.VISIBLE }
    private fun hideResult() { resultCard.visibility = View.GONE; currentVideoUrl = null }
    
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
