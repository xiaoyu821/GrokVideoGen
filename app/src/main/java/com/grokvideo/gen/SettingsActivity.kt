package com.grokvideo.gen

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.grokvideo.gen.data.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var baseUrlInput: EditText
    private lateinit var apiKeyInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var saveButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        prefsManager = PreferencesManager(this)
        
        initViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initViews() {
        baseUrlInput = findViewById(R.id.baseUrlInput)
        apiKeyInput = findViewById(R.id.apiKeyInput)
        modelInput = findViewById(R.id.modelInput)
        saveButton = findViewById(R.id.saveButton)
    }
    
    private fun loadSettings() {
        baseUrlInput.setText(prefsManager.baseUrl)
        apiKeyInput.setText(prefsManager.apiKey)
        modelInput.setText(prefsManager.model)
    }
    
    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        val baseUrl = baseUrlInput.text.toString().trim()
        val apiKey = apiKeyInput.text.toString().trim()
        val model = modelInput.text.toString().trim()
        
        if (baseUrl.isNotBlank()) prefsManager.baseUrl = baseUrl
        if (apiKey.isNotBlank()) prefsManager.apiKey = apiKey
        if (model.isNotBlank()) prefsManager.model = model
        
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
