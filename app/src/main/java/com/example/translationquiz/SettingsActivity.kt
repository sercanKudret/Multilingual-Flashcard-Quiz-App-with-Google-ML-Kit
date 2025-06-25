package com.example.translationquiz

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonLanguageSelector: MaterialButton
    private lateinit var buttonDownloadModel: MaterialButton

    private var selectedLanguage: String? = null

    // İndirme ilerlemesi için ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        buttonLanguageSelector = findViewById(R.id.buttonLanguageSelector)
        buttonDownloadModel = findViewById(R.id.buttonDownloadModel)

        val supportedLanguages = TranslateLanguage.getAllLanguages().map { code ->
            Locale(code).displayName
        }

        // Dil Seçimi
        buttonLanguageSelector.setOnClickListener {
            showLanguageSelectionDialog(
                "Dil Seç",
                supportedLanguages
            ) { selectedLanguageName ->
                selectedLanguage = TranslateLanguage.fromLanguageTag(
                    Locale.getISOLanguages().find { Locale(it).displayName == selectedLanguageName } ?: "en"
                )
                buttonLanguageSelector.text = selectedLanguageName
            }
        }

        // Model İndirme
        buttonDownloadModel.setOnClickListener {
            if (selectedLanguage == null) {
                Toast.makeText(
                    this,
                    "Lütfen bir dil seçin!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                downloadLanguageModel(selectedLanguage!!)
            }
        }
    }

    /**
     * Dil Seçim Dialogu
     */
    private fun showLanguageSelectionDialog(
        title: String,
        languages: List<String>,
        onLanguageSelected: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(languages.toTypedArray()) { _, which ->
            onLanguageSelected(languages[which])
        }
        builder.show()
    }

    /**
     * Dil Modeli Kontrol ve İndirme
     */
    private fun downloadLanguageModel(language: String) {
        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(language).build()

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Kullanıcı arayüzünü kilitle
        setUiEnabled(false)
        showProgressDialog("Dil indiriliyor...")

        modelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    progressDialog.dismiss()
                    setUiEnabled(true)
                    Toast.makeText(
                        this,
                        "$language modeli zaten yüklü.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    modelManager.download(model, conditions)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            setUiEnabled(true)
                            Toast.makeText(
                                this,
                                "$language modeli başarıyla indirildi.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            setUiEnabled(true)
                            Toast.makeText(
                                this,
                                "Model indirme başarısız: ${exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                setUiEnabled(true)
                Toast.makeText(
                    this,
                    "Model durumu kontrolü başarısız: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * ProgressDialog Göster
     */
    private fun showProgressDialog(message: String) {
        progressDialog = ProgressDialog(this).apply {
            setTitle("Lütfen Bekleyin")
            setMessage(message)
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            show()
        }
    }

    /**
     * Kullanıcı Arayüzünü Etkin/Pasif Hale Getir
     */
    private fun setUiEnabled(enabled: Boolean) {
        buttonLanguageSelector.isEnabled = enabled
        buttonDownloadModel.isEnabled = enabled
    }
}
