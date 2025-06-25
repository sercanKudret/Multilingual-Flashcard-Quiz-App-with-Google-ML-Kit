package com.example.translationquiz

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.translationquiz.database.SQLiteHelper
import com.google.android.material.snackbar.Snackbar

class WordListActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLiteHelper
    private lateinit var layoutContainer: LinearLayout

    private var sourceLanguage: String = ""
    private var targetLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        dbHelper = SQLiteHelper(this)
        layoutContainer = findViewById(R.id.layoutContainer)

        // Intent'ten gelen verileri al
        sourceLanguage = intent.getStringExtra("sourceLanguage") ?: ""
        targetLanguage = intent.getStringExtra("targetLanguage") ?: ""

        if (sourceLanguage.isEmpty() || targetLanguage.isEmpty()) {
            Toast.makeText(this, "Dil bilgileri eksik!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            displayWordList()
        }
    }

    /**
     * Veritabanından kelime çiftlerini alır ve listeler.
     */
    private fun displayWordList() {
        val wordList = dbHelper.getTranslationsForCategory(sourceLanguage, targetLanguage)
        layoutContainer.removeAllViews()

        if (wordList.isEmpty()) {
            Toast.makeText(this, "Bu kategoriye ait kelime bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        for ((sourceText, targetText) in wordList) {
            addWordWidget(sourceText, targetText)
        }
    }

    /**
     * Tek bir kelime widget'ı oluşturur ve ekler.
     */
    private fun addWordWidget(sourceText: String, targetText: String) {
        val widget = layoutInflater.inflate(R.layout.base_item, layoutContainer, false)

        val textViewSource = widget.findViewById<TextView>(R.id.textViewSource)
        val textViewTarget = widget.findViewById<TextView>(R.id.textViewTarget)
        val buttonDelete = widget.findViewById<ImageButton>(R.id.buttonDelete)

        textViewSource.text = sourceText
        textViewTarget.text = targetText

        // Silme butonuna tıklama işlemi
        buttonDelete.setOnClickListener {
            val result = dbHelper.deleteTranslation(sourceLanguage, targetLanguage, sourceText, targetText)
            if (result > 0) {
                layoutContainer.removeView(widget)
                Snackbar.make(layoutContainer, "Kelime başarıyla silindi!", Snackbar.LENGTH_SHORT).show()
                setResult(RESULT_OK, Intent().apply { putExtra("updateRequired", true) })
            } else {
                Snackbar.make(layoutContainer, "Kelime silinemedi!", Snackbar.LENGTH_SHORT).show()
            }
        }

        layoutContainer.addView(widget)
    }
}
