package com.grokvideo.gen

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
import com.google.android.material.color.DynamicColors
import com.grokvideo.gen.data.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var historyManager: HistoryManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        historyManager = HistoryManager(this)
        
        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        loadHistory()
    }
    
    private fun loadHistory() {
        val tasks = historyManager.loadHistory()
        if (tasks.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            recyclerView.adapter = HistoryAdapter(tasks) { task ->
                AlertDialog.Builder(this)
                    .setTitle("删除")
                    .setMessage("确定要删除这条记录吗？")
                    .setPositiveButton("删除") { _, _ -> historyManager.deleteTask(task.id); loadHistory() }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }
    
    inner class HistoryAdapter(
        private val tasks: List<VideoTask>,
        private val onDelete: (VideoTask) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.VH>() {
        private val df = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(tasks[position])
        override fun getItemCount() = tasks.size
        
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val promptText: TextView = v.findViewById(R.id.promptText)
            private val dateText: TextView = v.findViewById(R.id.dateText)
            private val deleteBtn: ImageButton = v.findViewById(R.id.deleteButton)
            private val prog: ProgressBar = v.findViewById(R.id.itemProgressBar)
            private val statusText: TextView = v.findViewById(R.id.statusText)
            private val actions: LinearLayout = v.findViewById(R.id.actionButtons)
            private val playBtn: Button = v.findViewById(R.id.itemPlayButton)
            private val durText: TextView = v.findViewById(R.id.durationText)
            
            fun bind(t: VideoTask) {
                promptText.text = t.prompt
                dateText.text = df.format(Date(t.createdAt))
                deleteBtn.setOnClickListener { onDelete(t) }
                when (t.status) {
                    TaskStatus.DONE -> {
                        prog.visibility = View.GONE; statusText.visibility = View.GONE
                        if (t.videoUrl != null) {
                            actions.visibility = View.VISIBLE
                            playBtn.setOnClickListener { itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(t.videoUrl))) }
                            durText.visibility = if (t.duration != null) View.VISIBLE else View.GONE
                            if (t.duration != null) durText.text = "${t.duration}秒"
                        }
                    }
                    TaskStatus.PROCESSING -> { prog.visibility = View.VISIBLE; prog.progress = t.progress; statusText.visibility = View.VISIBLE; statusText.text = "生成中 ${t.progress}%"; actions.visibility = View.GONE }
                    TaskStatus.FAILED -> { prog.visibility = View.GONE; statusText.visibility = View.VISIBLE; statusText.text = "❌ 生成失败"; actions.visibility = View.GONE }
                    TaskStatus.PENDING -> { prog.visibility = View.VISIBLE; prog.isIndeterminate = true; statusText.visibility = View.VISIBLE; statusText.text = "⏳ 等待中..."; actions.visibility = View.GONE }
                }
            }
        }
    }
}
