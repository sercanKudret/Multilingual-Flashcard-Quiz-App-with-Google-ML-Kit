# 📱 Multilingual Flashcard & Quiz App with Google ML Kit

This is a mobile language learning app built using Kotlin and Android Studio. It utilizes **Google ML Kit's Translation API** to translate words between multiple languages in real-time.

## ✨ Features

- 🌐 **Multi-language translation** with Google ML Kit
- 💾 **Save translated words** for future reference
- 🧠 **Quiz mode**: Practice saved words through multiple-choice questions
- 🎤 **Voice input**: Speak instead of typing
- 🔊 **Text-to-speech**: Hear the correct pronunciation of any word
- 🔀 **Randomized quiz questions** based on saved vocabulary

## 🧪 How Quiz Mode Works

1. You save words using the translator.
2. During quiz mode, the app selects random words from your saved list.
3. For each question, a word is shown (e.g., **"hi"**), and you choose the correct translation from 4 options (e.g., *food*, *sick*, *hello*, *friend*).
4. Select the correct answer to move to the next question.
5. Incorrect answers do not advance — it helps reinforce learning.

## 🔧 Tech Stack

- Kotlin & Android Studio
- Google ML Kit (Translation API)
- Text-to-Speech & Speech Recognition (Android)
- Local Storage / SharedPreferences / SQLite (optional for saving words)
