package com.grokvideo.gen

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.grokvideo.gen.data.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var baseUrlInput: EditText
    private lateinit var apiKeyInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefsManager = PreferencesManager(this)
        
        baseUrlInput = findViewById(R.id.baseUrlInput)
        apiKeyInput = findViewById(R.id.apiKeyInput)
        modelInput = findViewById(R.id.modelInput)
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        
        baseUrlInput.setText(prefsManager.baseUrl)
        apiKeyInput.setText(prefsManager.apiKey)
        modelInput.setText(prefsManager.model)
        
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val baseUrl = baseUrlInput.text.toString().trim()
            val apiKey = apiKeyInput.text.toString().trim()
            val model = modelInput.text.toString().trim()
            
            if (baseUrl.isNotBlank()) prefsManager.baseUrl = baseUrl
            if (apiKey.isNotBlank()) prefsManager.apiKey = apiKey
            if (model.isNotBlank()) prefsManager.model = model
            
            Toast.makeText(this, "✅ 已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
