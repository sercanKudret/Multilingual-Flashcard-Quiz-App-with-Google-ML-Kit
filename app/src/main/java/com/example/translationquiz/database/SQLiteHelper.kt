package com.example.translationquiz.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "translations.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TRANSLATIONS = "translations"
        private const val COLUMN_ID = "id"
        private const val COLUMN_SOURCE_LANGUAGE = "source_language"
        private const val COLUMN_TARGET_LANGUAGE = "target_language"
        private const val COLUMN_SOURCE_TEXT = "source_text"
        private const val COLUMN_TARGET_TEXT = "target_text"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRANSLATIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SOURCE_LANGUAGE TEXT,
                $COLUMN_TARGET_LANGUAGE TEXT,
                $COLUMN_SOURCE_TEXT TEXT,
                $COLUMN_TARGET_TEXT TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSLATIONS")
        onCreate(db)
    }

    /**
     * Çeviri çiftini veritabanına ekler.
     */
    fun insertTranslation(
        sourceLanguage: String,
        targetLanguage: String,
        sourceText: String,
        targetText: String
    ): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SOURCE_LANGUAGE, sourceLanguage)
            put(COLUMN_TARGET_LANGUAGE, targetLanguage)
            put(COLUMN_SOURCE_TEXT, sourceText)
            put(COLUMN_TARGET_TEXT, targetText)
        }

        val result = db.insert(TABLE_TRANSLATIONS, null, contentValues)
        db.close()
        return result
    }


    fun getWordCountForPair(sourceLang: String, targetLang: String): Int {
        val db = this.readableDatabase
        val query = """
        SELECT COUNT(*) as word_count 
        FROM translations 
        WHERE source_language = ? AND target_language = ?
    """
        val cursor = db.rawQuery(query, arrayOf(sourceLang, targetLang))
        var count = 0

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("word_count"))
        }
        cursor.close()
        db.close()
        return count
    }

    fun getLanguagePairsWithWordCount(): List<Pair<String, Int>> {
        val pairs = mutableListOf<Pair<String, Int>>()
        val db = this.readableDatabase
        val query = """
        SELECT $COLUMN_SOURCE_LANGUAGE, $COLUMN_TARGET_LANGUAGE, COUNT(*) as word_count 
        FROM $TABLE_TRANSLATIONS 
        GROUP BY $COLUMN_SOURCE_LANGUAGE, $COLUMN_TARGET_LANGUAGE
    """
        val cursor = db.rawQuery(query, null)

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val sourceLanguage = it.getString(it.getColumnIndexOrThrow(COLUMN_SOURCE_LANGUAGE))
                    val targetLanguage = it.getString(it.getColumnIndexOrThrow(COLUMN_TARGET_LANGUAGE))
                    val wordCount = it.getInt(it.getColumnIndexOrThrow("word_count"))
                    pairs.add(Pair("$sourceLanguage -> $targetLanguage", wordCount))
                } while (it.moveToNext())
            }
        }
        db.close()
        return pairs
    }

    fun getTranslationsForCategory(sourceLang: String, targetLang: String): List<Pair<String, String>> {
        val wordPairs = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val query = """
        SELECT source_text, target_text 
        FROM translations 
        WHERE source_language = ? AND target_language = ?
    """
        val cursor = db.rawQuery(query, arrayOf(sourceLang, targetLang))

        if (cursor.moveToFirst()) {
            do {
                val sourceText = cursor.getString(cursor.getColumnIndexOrThrow("source_text"))
                val targetText = cursor.getString(cursor.getColumnIndexOrThrow("target_text"))
                wordPairs.add(Pair(sourceText, targetText))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return wordPairs
    }

    fun getRandomQuestionWithOptions(sourceLang: String, targetLang: String): Pair<Pair<String, String>, List<String>>? {
        val db = this.readableDatabase

        // Rastgele bir soru seç
        val questionCursor = db.rawQuery("""
        SELECT source_text, target_text 
        FROM translations 
        WHERE source_language = ? AND target_language = ? 
        ORDER BY RANDOM() LIMIT 1
    """, arrayOf(sourceLang, targetLang))

        if (!questionCursor.moveToFirst()) {
            questionCursor.close()
            db.close()
            return null
        }

        val question = questionCursor.getString(questionCursor.getColumnIndexOrThrow("source_text"))
        val correctAnswer = questionCursor.getString(questionCursor.getColumnIndexOrThrow("target_text"))
        questionCursor.close()

        // Rastgele 3 yanlış cevap seç
        val optionsCursor = db.rawQuery("""
        SELECT target_text 
        FROM translations 
        WHERE source_language = ? AND target_language = ? AND target_text != ?
        ORDER BY RANDOM() LIMIT 3
    """, arrayOf(sourceLang, targetLang, correctAnswer))

        val options = mutableListOf(correctAnswer)
        while (optionsCursor.moveToNext()) {
            options.add(optionsCursor.getString(optionsCursor.getColumnIndexOrThrow("target_text")))
        }
        optionsCursor.close()
        db.close()

        return Pair(Pair(question, correctAnswer), options.shuffled())
    }

    fun isTranslationExists(sourceLang: String, targetLang: String, sourceText: String, targetText: String): Boolean {
        val db = this.readableDatabase
        var exists = false
        val query = """
        SELECT COUNT(*) FROM $TABLE_TRANSLATIONS 
        WHERE $COLUMN_SOURCE_LANGUAGE = ? AND $COLUMN_TARGET_LANGUAGE = ? 
        AND $COLUMN_SOURCE_TEXT = ? AND $COLUMN_TARGET_TEXT = ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(sourceLang, targetLang, sourceText, targetText)).use { cursor ->
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0
            }
        }
        db.close()
        return exists
    }

    /**
     * Belirli bir çeviri çiftini siler.
     */
    fun deleteTranslation(sourceLang: String, targetLang: String, sourceText: String, targetText: String): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_TRANSLATIONS,
            "$COLUMN_SOURCE_LANGUAGE = ? AND $COLUMN_TARGET_LANGUAGE = ? AND $COLUMN_SOURCE_TEXT = ? AND $COLUMN_TARGET_TEXT = ?",
            arrayOf(sourceLang, targetLang, sourceText, targetText)
        )
        db.close()
        return result
    }


}
