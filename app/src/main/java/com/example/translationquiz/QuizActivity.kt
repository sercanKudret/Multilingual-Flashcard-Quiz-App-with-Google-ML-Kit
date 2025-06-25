package com.example.translationquiz

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.translationquiz.database.SQLiteHelper

class QuizActivity : AppCompatActivity() {

    // UI Bileşenleri
    private lateinit var textViewQuestion: TextView
    private lateinit var cardOption1: CardView
    private lateinit var cardOption2: CardView
    private lateinit var cardOption3: CardView
    private lateinit var cardOption4: CardView
    private lateinit var textViewOption1: TextView
    private lateinit var textViewOption2: TextView
    private lateinit var textViewOption3: TextView
    private lateinit var textViewOption4: TextView

    // Diğer değişkenler
    private lateinit var dbHelper: SQLiteHelper
    private var sourceLanguage: String = ""
    private var targetLanguage: String = ""
    private var correctAnswer: String = ""
    private var wordCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // UI Bileşenlerini Bağla
        textViewQuestion = findViewById(R.id.textViewQuestion)

        cardOption1 = findViewById(R.id.cardOption1)
        cardOption2 = findViewById(R.id.cardOption2)
        cardOption3 = findViewById(R.id.cardOption3)
        cardOption4 = findViewById(R.id.cardOption4)

        textViewOption1 = findViewById(R.id.textViewOption1)
        textViewOption2 = findViewById(R.id.textViewOption2)
        textViewOption3 = findViewById(R.id.textViewOption3)
        textViewOption4 = findViewById(R.id.textViewOption4)

        dbHelper = SQLiteHelper(this)

        // Intent'ten gelen verileri al
        sourceLanguage = intent.getStringExtra("sourceLanguage") ?: ""
        targetLanguage = intent.getStringExtra("targetLanguage") ?: ""
        wordCount = intent.getIntExtra("wordCount", 0)

        Log.d("QuizActivity", "Source: $sourceLanguage, Target: $targetLanguage, WordCount: $wordCount")

        if (wordCount < 4) {
            showInsufficientWordsDialog()
        } else {
            startQuiz()
        }

        // Kartlara Tıklama Olayları
        cardOption1.setOnClickListener { checkAnswer(textViewOption1.text.toString(), cardOption1) }
        cardOption2.setOnClickListener { checkAnswer(textViewOption2.text.toString(), cardOption2) }
        cardOption3.setOnClickListener { checkAnswer(textViewOption3.text.toString(), cardOption3) }
        cardOption4.setOnClickListener { checkAnswer(textViewOption4.text.toString(), cardOption4) }
    }

    /**
     * Yetersiz Kelime Uyarısı
     */
    private fun showInsufficientWordsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Yetersiz Kelime Sayısı")
            .setMessage("Quiz başlatmak için en az 4 kelime çifti kaydedilmelidir.")
            .setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Quiz başlatma işlemleri
     */
    private fun startQuiz() {
        loadQuestion()
    }

    /**
     * Rastgele Soru Yükle
     */
    private fun loadQuestion() {
        val questionData = dbHelper.getRandomQuestionWithOptions(sourceLanguage, targetLanguage)

        if (questionData != null) {
            val (questionPair, options) = questionData
            textViewQuestion.text = questionPair.first
            correctAnswer = questionPair.second

            val cards = listOf(cardOption1, cardOption2, cardOption3, cardOption4)
            val optionTexts = listOf(textViewOption1, textViewOption2, textViewOption3, textViewOption4)

            optionTexts.forEachIndexed { index, textView ->
                textView.text = options[index]
                cards[index].setCardBackgroundColor(resources.getColor(android.R.color.white))
            }
        } else {
            Toast.makeText(this, "Bu kategori için soru bulunamadı!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Şıkları etkinleştirir veya devre dışı bırakır.
     */
    private fun setOptionsEnabled(enabled: Boolean) {
        val cards = listOf(cardOption1, cardOption2, cardOption3, cardOption4)
        cards.forEach { it.isEnabled = enabled }
    }

    /**
     * Cevabı Kontrol Et
     */
    private fun checkAnswer(selectedAnswer: String, selectedCard: CardView) {
        // Tıklamaları devre dışı bırak
        setOptionsEnabled(false)

        val cards = listOf(cardOption1, cardOption2, cardOption3, cardOption4)
        val optionTexts = listOf(textViewOption1, textViewOption2, textViewOption3, textViewOption4)

        if (selectedAnswer == correctAnswer) {
            selectedCard.setCardBackgroundColor(resources.getColor(R.color.correct_answer))
            Toast.makeText(this, "Doğru cevap!", Toast.LENGTH_SHORT).show()
        } else {
            selectedCard.setCardBackgroundColor(resources.getColor(R.color.wrong_answer))
            Toast.makeText(this, "Yanlış cevap!", Toast.LENGTH_SHORT).show()

            cards.zip(optionTexts).find { it.second.text == correctAnswer }?.first
                ?.setCardBackgroundColor(resources.getColor(R.color.correct_answer))
        }

        selectedCard.postDelayed({
            resetCardColors()
            loadQuestion()
            setOptionsEnabled(true) // Yeni soru yüklendiğinde tekrar etkinleştir
        }, 2000)
    }


    /**
     * Kart Renklerini Sıfırla
     */
    private fun resetCardColors() {
        val cards = listOf(cardOption1, cardOption2, cardOption3, cardOption4)
        cards.forEach { it.setCardBackgroundColor(resources.getColor(android.R.color.white)) }
    }
}
