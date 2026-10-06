package com.grokvideo.gen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.grokvideo.gen.data.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var promptInput: EditText
    private lateinit var generateButton: Button
    private lateinit var progressLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var errorCard: CardView
    private lateinit var errorText: TextView
    private lateinit var resultCard: CardView
    private lateinit var videoThumbnail: ImageView
    private lateinit var playButton: Button
    private lateinit var downloadButton: Button
    
    private lateinit var apiService: ApiService
    private lateinit var prefsManager: PreferencesManager
    private lateinit var historyManager: HistoryManager
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentVideoUrl: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
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
    }
    
    private fun setupListeners() {
        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isBlank()) {
                showError(getString(R.string.error_empty_prompt))
                return@setOnClickListener
            }
            generateVideo(prompt)
        }
        
        playButton.setOnClickListener {
            currentVideoUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
        
        downloadButton.setOnClickListener {
            currentVideoUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setDataAndType(Uri.parse(url), "video/mp4")
                startActivity(Intent.createChooser(intent, "下载视频"))
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun generateVideo(prompt: String) {
        val apiKey = prefsManager.apiKey
        if (apiKey.isBlank()) {
            showError(getString(R.string.error_no_api_key))
            return
        }
        
        hideError()
        hideResult()
        showProgress()
        generateButton.isEnabled = false
        
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
                
                withContext(Dispatchers.IO) {
                    historyManager.addTask(task)
                }
                
                pollTaskStatus(baseUrl, apiKey, taskId, task)
                
            } catch (e: Exception) {
                hideProgress()
                generateButton.isEnabled = true
                showError(e.message ?: "未知错误")
            }
        }
    }
    
    private suspend fun pollTaskStatus(baseUrl: String, apiKey: String, taskId: String, task: VideoTask) {
        var attempts = 0
        val maxAttempts = 60
        
        while (attempts < maxAttempts) {
            delay(5000)
            attempts++
            
            try {
                val statusResponse = withContext(Dispatchers.IO) {
                    apiService.checkTaskStatus(baseUrl, apiKey, taskId)
                }
                
                val progress = statusResponse.progress
                updateProgress(progress)
                
                val updatedTask = task.copy(
                    status = when (statusResponse.status) {
                        "done" -> TaskStatus.DONE
                        "failed" -> TaskStatus.FAILED
                        else -> TaskStatus.PROCESSING
                    },
                    progress = progress,
                    videoUrl = statusResponse.video?.url,
                    duration = statusResponse.video?.duration,
                    completedAt = if (statusResponse.status == "done") System.currentTimeMillis() else null
                )
                
                withContext(Dispatchers.IO) {
                    historyManager.updateTask(updatedTask)
                }
                
                when (statusResponse.status) {
                    "done" -> {
                        hideProgress()
                        generateButton.isEnabled = true
                        showResult(statusResponse.video?.url)
                        return
                    }
                    "failed" -> {
                        throw Exception("视频生成失败")
                    }
                }
                
            } catch (e: Exception) {
                hideProgress()
                generateButton.isEnabled = true
                showError(e.message ?: "未知错误")
                return
            }
        }
        
        hideProgress()
        generateButton.isEnabled = true
        showError("生成超时，请稍后在历史记录中查看")
    }
    
    private fun showProgress() {
        progressLayout.visibility = View.VISIBLE
        progressBar.progress = 0
        progressText.text = "0%"
    }
    
    private fun hideProgress() {
        progressLayout.visibility = View.GONE
    }
    
    private fun updateProgress(progress: Int) {
        progressBar.progress = progress
        progressText.text = "$progress%"
    }
    
    private fun showError(message: String) {
        errorCard.visibility = View.VISIBLE
        errorText.text = message
    }
    
    private fun hideError() {
        errorCard.visibility = View.GONE
    }
    
    private fun showResult(videoUrl: String?) {
        if (videoUrl == null) return
        currentVideoUrl = videoUrl
        resultCard.visibility = View.VISIBLE
    }
    
    private fun hideResult() {
        resultCard.visibility = View.GONE
        currentVideoUrl = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
