package com.example.translationquiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.example.translationquiz.database.SQLiteHelper
import java.util.Locale

class WordLibraryActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLiteHelper
    private lateinit var layoutContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_library)

        dbHelper = SQLiteHelper(this)
        layoutContainer = findViewById(R.id.layoutContainer)

        displayLanguagePairs()
    }

    /**
     * Dil çiftlerini ve kelime sayılarını listeler.
     */
    private fun displayLanguagePairs() {
        val languagePairs = dbHelper.getLanguagePairsWithWordCount()
        layoutContainer.removeAllViews()

        if (languagePairs.isEmpty()) {
            Toast.makeText(this, "Henüz bir kelime kaydedilmedi.", Toast.LENGTH_SHORT).show()
            return
        }

        for (pair in languagePairs) {
            addLanguagePairWidget(pair.first, pair.second)
        }
    }

    /**
     * Tek bir dil çifti widget'ı oluşturur.
     */
    private fun addLanguagePairWidget(languagePair: String, wordCount: Int) {
        // BaseWidget XML dosyasını şişir (inflate)
        val widget = layoutInflater.inflate(R.layout.base_widget, layoutContainer, false)

        val cardWidget = widget.findViewById<CardView>(R.id.cardWidget)
        val buttonAction = widget.findViewById<MaterialButton>(R.id.buttonWidgetAction)
        val textViewLanguagePair = widget.findViewById<TextView>(R.id.textViewLanguagePair)
        val textViewWordCount = widget.findViewById<TextView>(R.id.textViewWordCount)

        // Dil çiftini ayır ve isimleri al
        val sourceLang = languagePair.split(" -> ")[0]
        val targetLang = languagePair.split(" -> ")[1]

        val sourceLangName = getLanguageName(sourceLang)
        val targetLangName = getLanguageName(targetLang)

        // Widget verilerini doldur
        textViewLanguagePair.text = "$sourceLangName → $targetLangName"
        textViewWordCount.text = "$wordCount kelime çifti"

        // Sol taraf (Widget): QuizActivity açılır
        cardWidget.setOnClickListener {
            Log.d("WidgetClick", "Widget clicked: $sourceLangName → $targetLangName")
            Toast.makeText(this, "$sourceLangName → $targetLangName seçildi", Toast.LENGTH_SHORT).show()

            // Veri tabanından kelime çiftlerinin sayısını al
            val wordCount = dbHelper.getWordCountForPair(sourceLang, targetLang)
            Log.d("WidgetClick", "Word count for $sourceLang -> $targetLang: $wordCount")

            // Intent ile bilgileri gönder
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("sourceLanguage", sourceLang)
            intent.putExtra("targetLanguage", targetLang)
            intent.putExtra("wordCount", wordCount)
            intent.putExtra("sourceLanguageName", sourceLangName) // Dil ismini ekledik
            intent.putExtra("targetLanguageName", targetLangName) // Dil ismini ekledik
            startActivity(intent)
        }

        // Sağ taraf (Buton): Detay görüntüleme işlemi
        buttonAction.setOnClickListener {
            Log.d("WordLibrary", "Göster Butonu Tıklandı: $sourceLang → $targetLang")
            Toast.makeText(this, "$sourceLangName → $targetLangName detayları görüntüleniyor.", Toast.LENGTH_SHORT).show()

            // Intent ile kategori bilgilerini WordListActivity'ye gönder
            val intent = Intent(this, WordListActivity::class.java)
            intent.putExtra("sourceLanguage", sourceLang)
            intent.putExtra("targetLanguage", targetLang)
            startActivity(intent)
        }


        // Widget'ı ana LinearLayout'a ekle
        layoutContainer.addView(widget)
    }

    /**
     * Yeni bir veri eklendiğinde listeyi günceller.
     */
    fun refreshLanguagePairs() {
        displayLanguagePairs()
    }

    /**
     * Dil kodunu dil ismine çevirir.
     */
    private fun getLanguageName(languageCode: String): String {
        return Locale(languageCode).displayName.replaceFirstChar { it.uppercase() }
    }

    override fun onResume() {
        super.onResume()
        refreshLanguagePairs()
    }

}
