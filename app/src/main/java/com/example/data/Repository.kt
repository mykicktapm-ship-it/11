package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssistantRepository(private val database: AppDatabase) {
    private val chatDao = database.chatDao()
    private val noteDao = database.noteDao()

    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insertMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    suspend fun insertNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    suspend fun getGeminiResponse(
        prompt: String,
        systemPrompt: String? = null,
        history: List<ChatMessageEntity> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Ошибка: API-ключ Gemini не настроен. Пожалуйста, добавьте GEMINI_API_KEY в панели Secrets приложения AI Studio."
        }

        val contents = mutableListOf<Content>()
        
        // Add chat history to context, limiting to avoid exceeding limits
        val limitedHistory = history.takeLast(10)
        for (msg in limitedHistory) {
            val prefix = if (msg.sender == "user") "Пользователь: " else "Ассистент: "
            contents.add(Content(parts = listOf(Part(text = "$prefix${msg.text}"))))
        }
        
        // Add current prompt
        contents.add(Content(parts = listOf(Part(text = prompt))))

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = systemPrompt ?: "Вы — умный, вежливый и невероятно полезный карманный ИИ-Ассистент по имени Aura. Отвечайте всегда на том языке, на котором обратился пользователь. Всячески старайтесь излагать кратко, понятно и аккуратно структурированно."
                )
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            reply ?: "Извините, не удалось получить пустой ответ от ИИ."
        } catch (e: Exception) {
            e.printStackTrace()
            "Ошибка при получении ответа от Gemini API: ${e.localizedMessage ?: e.message}"
        }
    }
}
