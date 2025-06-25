package com.example.translationquiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardTranslation = findViewById<CardView>(R.id.cardTranslation)
        val cardWordLibrary = findViewById<CardView>(R.id.cardWordLibrary)
        val cardSettings = findViewById<CardView>(R.id.cardSettings)

        cardTranslation.setOnClickListener {
            val intent = Intent(this, TranslationActivity::class.java)
            startActivity(intent)
        }

        cardWordLibrary.setOnClickListener {
            val intent = Intent(this, WordLibraryActivity::class.java)
            startActivity(intent)
        }

        cardSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
