package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AssistantRepository
import com.example.data.ChatMessageEntity
import com.example.data.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(private val repository: AssistantRepository) : ViewModel() {

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _aiOperationResult = MutableStateFlow<String?>(null)
    val aiOperationResult: StateFlow<String?> = _aiOperationResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun updatePrompt(prompt: String) {
        _currentPrompt.value = prompt
    }

    fun sendMessage() {
        val promptText = _currentPrompt.value.trim()
        if (promptText.isEmpty()) return

        _currentPrompt.value = ""
        _isChatLoading.value = true

        viewModelScope.launch {
            // Save user message
            val userMsg = ChatMessageEntity(sender = "user", text = promptText)
            repository.insertMessage(userMsg)

            // Query Gemini
            val history = messages.value
            val aiReply = repository.getGeminiResponse(prompt = promptText, history = history)

            // Save assistant message
            val assistantMsg = ChatMessageEntity(sender = "assistant", text = aiReply)
            repository.insertMessage(assistantMsg)

            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Insert a friendly welcome message
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "assistant",
                    text = "Привет! Я твой ИИ-Ассистент Aura. Как я могу тебе сэкономить время или помочь зафиксировать умные мысли сегодня? 💬✏️"
                )
            )
        }
    }

    fun prefillWelcomeIfNeeded() {
        viewModelScope.launch {
            if (messages.value.isEmpty()) {
                repository.insertMessage(
                    ChatMessageEntity(
                        sender = "assistant",
                        text = "Привет! Я твой ИИ-Ассистент Aura. Я умею отвечать на вопросы, помогать планировать дела и прокачивать твои заметки с помощью Gemini ИИ. 💬✏️"
                    )
                )
            }
        }
    }

    // --- Notes operations ---
    fun addNote(title: String, content: String, category: String, colorHex: String) {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title.ifBlank { "Заметка" },
                content = content,
                category = category.ifBlank { "Общее" },
                colorHex = colorHex
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    // --- AI Smart Operations on Notes ---
    fun clearAiOperationResult() {
        _aiOperationResult.value = null
    }

    fun processNoteWithAi(note: NoteEntity, actionType: String) {
        _isAiLoading.value = true
        _aiOperationResult.value = null

        viewModelScope.launch {
            val systemPrompt = "Вы — профессиональный редактор и креативный эксперт. " +
                    "Форматируйте и улучшайте текст, делая его структурированным, грамотным и красивым. " +
                    "Обязательно пишите на русском языке."

            val userPrompt = when (actionType) {
                "summarize" -> "Сделай краткое резюме (summary) и выдели главное из этой заметки в виде пунктов списка:\n\n" +
                        "Заголовок: ${note.title}\n" +
                        "Текст зарисовки:\n${note.content}"
                "expand" -> "Напиши развернутую и красивую статью или подробную заметку на основе исходного черновика. " +
                        "Добавь интересные детали, структуру и выдержи профессиональный, приятный тон:\n\n" +
                        "Заголовок: ${note.title}\n" +
                        "Черновик:\n${note.content}"
                "todo" -> "Преврати этот текст в готовый список дел (To-Do List) с чек-боксами [ ] для выполнения. " +
                        "Раздели задачи по приоритетам или шагам:\n\n" +
                        "Заметка:\n${note.content}"
                "ideas" -> "На основе этой заметки сгенерируй 5 креативных идей, дополнений или альтернативных путей развития:\n\n" +
                        "Заголовок: ${note.title}\n" +
                        "Содержание:\n${note.content}"
                else -> "Проанализируй и улучши этот текст, исправив ошибки и сделав формулировки более профессиональными:\n\n" +
                        "Содержание:\n${note.content}"
            }

            val result = repository.getGeminiResponse(
                prompt = userPrompt,
                systemPrompt = systemPrompt,
                history = emptyList()
            )
            _aiOperationResult.value = result
            _isAiLoading.value = false
        }
    }

    suspend fun queryGeminiDirectly(prompt: String): String {
        return repository.getGeminiResponse(prompt)
    }
}

class AssistantViewModelFactory(private val repository: AssistantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
