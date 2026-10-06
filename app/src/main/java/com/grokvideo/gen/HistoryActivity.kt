package com.grokvideo.gen

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.grokvideo.gen.data.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var historyManager: HistoryManager
    private lateinit var adapter: HistoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        historyManager = HistoryManager(this)
        
        initViews()
        loadHistory()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadHistory() {
        val tasks = historyManager.loadHistory()
        
        if (tasks.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            
            adapter = HistoryAdapter(tasks) { task ->
                showDeleteDialog(task)
            }
            recyclerView.adapter = adapter
        }
    }
    
    private fun showDeleteDialog(task: VideoTask) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                historyManager.deleteTask(task.id)
                loadHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    inner class HistoryAdapter(
        private val tasks: List<VideoTask>,
        private val onDeleteClick: (VideoTask) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
        
        private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            holder.bind(task)
        }
        
        override fun getItemCount() = tasks.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val promptText: TextView = itemView.findViewById(R.id.promptText)
            private val dateText: TextView = itemView.findViewById(R.id.dateText)
            private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
            private val itemProgressBar: ProgressBar = itemView.findViewById(R.id.itemProgressBar)
            private val statusText: TextView = itemView.findViewById(R.id.statusText)
            private val itemThumbnail: ImageView = itemView.findViewById(R.id.itemThumbnail)
            private val actionButtons: LinearLayout = itemView.findViewById(R.id.actionButtons)
            private val itemPlayButton: MaterialButton = itemView.findViewById(R.id.itemPlayButton)
            private val durationText: TextView = itemView.findViewById(R.id.durationText)
            
            fun bind(task: VideoTask) {
                promptText.text = task.prompt
                dateText.text = dateFormat.format(Date(task.createdAt))
                
                deleteButton.setOnClickListener {
                    onDeleteClick(task)
                }
                
                when (task.status) {
                    TaskStatus.DONE -> {
                        itemProgressBar.visibility = View.GONE
                        statusText.visibility = View.GONE
                        
                        if (task.videoUrl != null) {
                            itemThumbnail.visibility = View.VISIBLE
                            actionButtons.visibility = View.VISIBLE
                            
                            Glide.with(itemView.context)
                                .load(task.videoUrl)
                                .into(itemThumbnail)
                            
                            itemPlayButton.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.videoUrl))
                                itemView.context.startActivity(intent)
                            }
                            
                            if (task.duration != null) {
                                durationText.visibility = View.VISIBLE
                                durationText.text = "${task.duration}秒"
                            } else {
                                durationText.visibility = View.GONE
                            }
                        }
                    }
                    TaskStatus.PROCESSING -> {
                        itemProgressBar.visibility = View.VISIBLE
                        itemProgressBar.progress = task.progress
                        statusText.visibility = View.VISIBLE
                        statusText.text = "生成中 ${task.progress}%"
                        itemThumbnail.visibility = View.GONE
                        actionButtons.visibility = View.GONE
                    }
                    TaskStatus.FAILED -> {
                        itemProgressBar.visibility = View.GONE
                        statusText.visibility = View.VISIBLE
                        statusText.text = "生成失败"
                        itemThumbnail.visibility = View.GONE
                        actionButtons.visibility = View.GONE
                    }
                    TaskStatus.PENDING -> {
                        itemProgressBar.visibility = View.VISIBLE
                        itemProgressBar.isIndeterminate = true
                        statusText.visibility = View.VISIBLE
                        statusText.text = "等待中..."
                        itemThumbnail.visibility = View.GONE
                        actionButtons.visibility = View.GONE
                    }
                }
            }
        }
    }
}
