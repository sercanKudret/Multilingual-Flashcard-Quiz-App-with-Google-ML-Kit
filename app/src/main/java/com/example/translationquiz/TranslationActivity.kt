package com.example.translationquiz

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.translationquiz.database.SQLiteHelper
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import java.util.Locale

class TranslationActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // UI Bileşenleri
    private lateinit var editTextSource: EditText
    private lateinit var buttonTranslate: Button
    private lateinit var buttonVoiceInput: ImageButton
    private lateinit var buttonSourceLanguage: Button
    private lateinit var buttonTargetLanguage: Button
    private lateinit var buttonSwapLanguages: ImageButton
    private lateinit var textViewTranslationResult: TextView
    private lateinit var buttonSave: Button

    // Çeviri ve TTS Bileşenleri
    private lateinit var translator: Translator
    private lateinit var textToSpeech: TextToSpeech

    private var sourceLanguage: String = "en"
    private var targetLanguage: String = "tr"

    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val REQUEST_CODE_MIC_PERMISSION = 200

    private lateinit var dbHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translation)

        // UI Bileşenlerini bağla
        editTextSource = findViewById(R.id.editTextSource)
        buttonTranslate = findViewById(R.id.buttonTranslate)
        buttonVoiceInput = findViewById(R.id.buttonVoiceInput)
        buttonSourceLanguage = findViewById(R.id.buttonSourceLanguage)
        buttonTargetLanguage = findViewById(R.id.buttonTargetLanguage)
        buttonSwapLanguages = findViewById(R.id.buttonSwapLanguages)
        textViewTranslationResult = findViewById(R.id.textViewTranslationResult)
        buttonSave = findViewById(R.id.buttonSave)

        // Text-to-Speech başlat
        textToSpeech = TextToSpeech(this, this)

        // Çeviri başlat
        setupTranslator()

        // Buton Tıklama İşlemleri
        buttonSourceLanguage.setOnClickListener { showLanguageDialog(isSource = true) }
        buttonTargetLanguage.setOnClickListener { showLanguageDialog(isSource = false) }
        buttonSwapLanguages.setOnClickListener { swapLanguages() }
        buttonTranslate.setOnClickListener { translateText() }
        buttonVoiceInput.setOnClickListener { checkMicrophonePermissionAndStartVoiceInput() }

        // TextView Tıklama İşlemi (Metin Seslendir)
        textViewTranslationResult.setOnClickListener { speakText() }

        dbHelper = SQLiteHelper(this)

        buttonSave.setOnClickListener {
            val sourceText = editTextSource.text.toString()
            val translatedText = textViewTranslationResult.text.toString()

            if (sourceText.isNotEmpty() && translatedText.isNotEmpty()) {
                if (dbHelper.isTranslationExists(sourceLanguage, targetLanguage, sourceText, translatedText)) {
                    Toast.makeText(this, "Bu kelime çifti zaten mevcut!", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.insertTranslation(
                        sourceLanguage,
                        targetLanguage,
                        sourceText,
                        translatedText
                    )
                    Toast.makeText(this, "Kelime başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kaydedilecek veri eksik!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     * Text-to-Speech başlatma durumu kontrolü
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale(targetLanguage))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Seçilen dil desteklenmiyor.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text-to-Speech başlatılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Çeviri istemcisini yapılandırır.
     */
    private fun setupTranslator() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
        translator = Translation.getClient(options)
    }

    /**
     * Kaynak ve hedef dili yer değiştirir.
     */
    private fun swapLanguages() {
        val temp = sourceLanguage
        sourceLanguage = targetLanguage
        targetLanguage = temp

        // Buton metinlerini dil adlarıyla güncelle
        buttonSourceLanguage.text = getLanguageName(sourceLanguage)
        buttonTargetLanguage.text = getLanguageName(targetLanguage)

        textViewTranslationResult.text = "" // Çıktıyı temizle

        setupTranslator()
    }

    /**
     * Metni çevirir.
     */
    private fun translateText() {
        val text = editTextSource.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(this, "Lütfen bir metin girin!", Toast.LENGTH_SHORT).show()
        } else {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    textViewTranslationResult.text = translatedText
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Çeviri başarısız: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    /**
     * Metni sesli okuma
     */
    private fun speakText() {
        val text = textViewTranslationResult.text.toString()
        if (text.isNotEmpty()) {
            textToSpeech.language = Locale(targetLanguage) // TTS dili hedef dile ayarlanıyor
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(this, "Okunacak metin bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dil seçim diyaloğu gösterir.
     */
    private fun showLanguageDialog(isSource: Boolean) {
        val remoteModelManager = RemoteModelManager.getInstance()
        remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                if (models.isEmpty()) {
                    Toast.makeText(this, "Henüz indirilen model yok.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Dil kodlarını ve isimlerini al
                val languageCodes = models.map { it.language }
                val languageNames = languageCodes.map { getLanguageName(it) }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle(if (isSource) "Kaynak Dil Seç" else "Hedef Dil Seç")
                    .setItems(languageNames) { _, which ->
                        val selectedLanguage = languageCodes[which]
                        if (isSource) {
                            sourceLanguage = selectedLanguage
                            buttonSourceLanguage.text = getLanguageName(selectedLanguage)
                        } else {
                            targetLanguage = selectedLanguage
                            buttonTargetLanguage.text = getLanguageName(selectedLanguage)
                        }
                        textViewTranslationResult.text = "" // Çıktıyı temizle
                        setupTranslator()
                    }
                    .show()
            }
    }

    /**
     * Mikrofon iznini kontrol eder ve sesli giriş başlatır.
     */
    private fun checkMicrophonePermissionAndStartVoiceInput() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_MIC_PERMISSION
            )
        } else {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşmaya başlayın...")

        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
    }

    override fun onDestroy() {
        super.onDestroy()
        translator.close()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            if (spokenText.isNotEmpty()) {
                editTextSource.setText(spokenText)
            } else {
                Toast.makeText(this, "Sesli girişten metin alınamadı.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLanguageName(languageCode: String): String {
        return Locale(languageCode).displayName.replaceFirstChar { it.uppercase() }
    }
}
