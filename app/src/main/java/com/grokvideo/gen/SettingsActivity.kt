package com.grokvideo.gen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.grokvideo.gen.data.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var baseUrlInput: TextInputEditText
    private lateinit var apiKeyInput: TextInputEditText
    private lateinit var modelInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setSupportActionBar(findViewById(R.id.toolbar))
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
        
        if (baseUrl.isNotBlank()) {
            prefsManager.baseUrl = baseUrl
        }
        if (apiKey.isNotBlank()) {
            prefsManager.apiKey = apiKey
        }
        if (model.isNotBlank()) {
            prefsManager.model = model
        }
        
        Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
