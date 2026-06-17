package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.data.NoteEntity
import kotlinx.coroutines.launch

// Color palettes for sleek modern look
val CosmicBg = Color(0xFF0F111E)
val CosmicSurface = Color(0xFF161A2D)
val CosmicAccent = Color(0xFF6B58F7)
val UserBubbleColor = Color(0xFF2A2D4A)
val AiBubbleColor = Color(0xFF1E1E34)

// Sticky Note Color Options
val NoteColors = listOf(
    Color(0xFFFFD2D2), // Soft Rose
    Color(0xFFD2E8FF), // Soft Blue
    Color(0xFFD2FFEA), // Soft Mint
    Color(0xFFFFF7C2), // Soft Amber
    Color(0xFFF0D2FF), // Soft Purple
    Color(0xFFE4E4E6)  // Cosmic Gray-White
)

fun Color.toHex(): String {
    return String.format("#%08X", this.value.toLong() and 0xFFFFFFFFL)
}

fun String.toColor(fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(viewModel: AssistantViewModel) {
    var activeTab by remember { mutableIntStateOf(0) }
    val messages by viewModel.messages.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val currentPrompt by viewModel.currentPrompt.collectAsState()
    
    // Bottom Sheets states
    var showAddNoteSheet by remember { mutableStateOf(false) }
    var selectedNoteForEdit by remember { mutableStateOf<NoteEntity?>(null) }
    
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.prefillWelcomeIfNeeded()
    }

    // Modern atmospheric background brush
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(CosmicBg, Color(0xFF07080F))
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = CosmicSurface,
                tonalElevation = 10.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 0) Icons.Filled.ChatBubble else Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Чат"
                        )
                    },
                    label = { Text("ИИ Ассистент", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = CosmicAccent,
                        unselectedIconColor = Color.LightGray.copy(alpha = 0.6f),
                        unselectedTextColor = Color.LightGray.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.FormatAlignLeft,
                            contentDescription = "Заметки"
                        )
                    },
                    label = { Text("Умные заметки", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = CosmicAccent,
                        unselectedIconColor = Color.LightGray.copy(alpha = 0.6f),
                        unselectedTextColor = Color.LightGray.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = "Идеи"
                        )
                    },
                    label = { Text("Помощник", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = CosmicAccent,
                        unselectedIconColor = Color.LightGray.copy(alpha = 0.6f),
                        unselectedTextColor = Color.LightGray.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (activeTab) {
                0 -> ChatTabScreen(
                    messages = messages,
                    isChatLoading = isChatLoading,
                    currentPrompt = currentPrompt,
                    onPromptChange = { viewModel.updatePrompt(it) },
                    onSend = {
                        viewModel.sendMessage()
                        keyboardController?.hide()
                    },
                    onClear = { viewModel.clearChat() }
                )
                1 -> NotesTabScreen(
                    notes = notes,
                    onAddClick = { showAddNoteSheet = true },
                    onNoteClick = { selectedNoteForEdit = it },
                    onTogglePin = { viewModel.togglePin(it) }
                )
                2 -> AssistantHubTabScreen(viewModel, notes)
            }

            // Sheet to create a new note
            if (showAddNoteSheet) {
                NoteCreationBottomSheet(
                    onDismiss = { showAddNoteSheet = false },
                    onSave = { title, content, cat, color ->
                        viewModel.addNote(title, content, cat, color)
                        showAddNoteSheet = false
                    }
                )
            }

            // Sheet to edit note + request AI smart operations!
            selectedNoteForEdit?.let { note ->
                NoteEditorBottomSheet(
                    note = note,
                    viewModel = viewModel,
                    onDismiss = { selectedNoteForEdit = null },
                    onSave = { updatedNote ->
                        viewModel.updateNote(updatedNote)
                        selectedNoteForEdit = null
                    },
                    onDelete = {
                        viewModel.deleteNote(note)
                        selectedNoteForEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun ChatTabScreen(
    messages: List<ChatMessageEntity>,
    isChatLoading: Boolean,
    currentPrompt: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onClear: () -> Unit
) {
    val listState = rememberLazyListState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Scroll to bottom when content changes
    LaunchedEffect(messages.size, isChatLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding)
    ) {
        // App Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AURA CHAT",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00FF88), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Карманный ИИ-Ассистент",
                        fontSize = 11.sp,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .background(CosmicSurface, RoundedCornerShape(12.dp))
                    .testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.ClearAll,
                    contentDescription = "Очистить чат",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }
        }

        // Message timeline
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty() && !isChatLoading) {
                // Empty state greeting
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Sparkle",
                        tint = CosmicAccent,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Вселенная знаний у тебя в кармане",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Спроси Aura о чем угодно — о планах на выходные, рецептах французских круассанов или о кодинге на Kotlin!",
                        color = Color.LightGray.copy(alpha = 0.61f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Quick suggested buttons
                    Text(
                        text = "Попробуй начать с вежливого вопроса:",
                        color = CosmicAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val suggestions = listOf(
                        "Посоветуй 5 книг по личному росту",
                        "Составь чек-лист дел на завтра",
                        "Объясни основы квантовой физики кратко"
                    )
                    suggestions.forEach { suggestion ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onPromptChange(suggestion) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(AiBubbleColor, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = CosmicAccent,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Aura подбирает слова...",
                                            fontSize = 13.sp,
                                            color = Color.LightGray.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chat Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentPrompt,
                onValueChange = onPromptChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("prompt_input_field"),
                placeholder = { Text("Задай сложный вопрос ИИ...", color = Color.Gray, fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CosmicSurface,
                    unfocusedContainerColor = CosmicSurface,
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = currentPrompt.isNotBlank() && !isChatLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (currentPrompt.isNotBlank() && !isChatLoading) ComicAccentBrush() else SolidGreyBrush(),
                        CircleShape
                    )
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Отправить",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleBg = if (isUser) UserBubbleColor else AiBubbleColor
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .background(bubbleBg, shape)
                .border(
                    width = 1.dp,
                    color = if (isUser) CosmicAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f),
                    shape = shape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "ВЫ" else "AURA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) CosmicAccent else Color(0xFF00D2FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun NotesTabScreen(
    notes: List<NoteEntity>,
    onAddClick: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ЗАМЕТКИ",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Интегрированная база твоих знаний",
                        fontSize = 11.sp,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (notes.isEmpty()) {
                // Empty notes state
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Empty Notes",
                        tint = CosmicAccent,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Здесь пока пусто",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Создавай заметки с идеями, списком покупок или планами, а ИИ-Ассистент поможет структурировать, составить таймлайн или расписать задачи в один клик!",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.NoteAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать заметку", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onTogglePin = { onTogglePin(note) }
                        )
                    }
                }
            }
        }

        // FAB to add new note
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_note_fab"),
            containerColor = CosmicAccent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NoteAdd,
                contentDescription = "Создать заметку",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    val cardColor = note.colorHex.toColor(fallback = Color(0xFFF0D2FF))
    val isLightNoteColor = true // Soft palette colors have high contrast with dark text

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinned) Color(0xFF1E1E2C) else Color.Black.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F111E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.content,
                    fontSize = 12.sp,
                    color = Color(0xFF1F243A),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun AssistantHubTabScreen(viewModel: AssistantViewModel, notes: List<NoteEntity>) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var displayAiAdvice by remember { mutableStateOf<String?>(null) }
    var isAdviceLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding)
            .padding(horizontal = 20.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ИИ ПОМОЩНИК",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Ежедневная доза мотивации и шаблоны текста",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily Motivation Card (Generative)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Generator",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ИИ МОТИВАЦИЯ ДНЯ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFFFB300),
                            letterSpacing = 1.sp
                        )
                    }

                    if (isAdviceLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CosmicAccent, strokeWidth = 2.dp)
                    } else {
                        Button(
                            onClick = {
                                isAdviceLoading = true
                                scope.launch {
                                    // Generate a custom motivational quote
                                    val prompt = "Сгенерируй одну короткую, глубокую, ободряющую и вдохновляющую цитату или напутствие на сегодняшний день. Сделай это стильно и лаконично (не более 3 предложений)."
                                    displayAiAdvice = viewModel.queryGeminiDirectly(prompt)
                                    isAdviceLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Получить", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = displayAiAdvice ?: "Нажмите кнопку выше, чтобы сгенерировать персональную цитату силы и продуктивности от нашего ИИ Aura на основе утреннего настроения!",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontStyle = if (displayAiAdvice == null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Template selector
        Text(
            text = "БЫСТРЫЕ ШАБЛОНЫ ЗАМЕТОК",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.LightGray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            val templates = listOf(
                NoteTemplate(
                    title = "План проекта",
                    desc = "Организовать шаги разработки продукта, цели и дедлайны.",
                    icon = Icons.Filled.EditCalendar,
                    cat = "Работа",
                    content = "Проект: \n\n1. Основные цели: \n2. Дедлайн: \n3. Список необходимых ресурсов: \n4. Ответственные:"
                ),
                NoteTemplate(
                    title = "Список задач ИИ",
                    desc = "Готовый каркас для занесения мелких дел с приоритетами.",
                    icon = Icons.Filled.TaskAlt,
                    cat = "Дела",
                    content = "[ ] КРИТИЧНО:\n- \n\n[ ] СДЕЛАТЬ СЕГОДНЯ:\n- \n\n[ ] НА НЕДЕЛЮ:\n-"
                ),
                NoteTemplate(
                    title = "Креативные брейнштормы",
                    desc = "Папка для внезапных идей, стартап-задумок и концепций.",
                    icon = Icons.Filled.AutoAwesome,
                    cat = "Идеи",
                    content = "Инициатива: \n\nСуть концепции: \n\nПочему это взлетит:\n\nПервые шаги:"
                )
            )

            items(templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.addNote(
                                title = template.title,
                                content = template.content,
                                category = template.cat,
                                colorHex = NoteColors.random().toHex()
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CosmicAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = template.icon,
                                contentDescription = null,
                                tint = CosmicAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = template.desc,
                                color = Color.LightGray.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.NoteAdd,
                            contentDescription = "Добавить",
                            tint = CosmicAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class NoteTemplate(
    val title: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val cat: String,
    val content: String
)

// --- Sheets, Modals, Forms ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationBottomSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Личное") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "НОВАЯ ЗАМЕТКА",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedLabelColor = CosmicAccent,
                    unfocusedLabelColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Текст заметки") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("note_content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedLabelColor = CosmicAccent,
                    unfocusedLabelColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category selector
            Text("КАТЕГОРИЯ ЗАМЕТКИ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicAccent)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = listOf("Личное", "Работа", "Идеи", "Дела")
                cats.forEach { cat ->
                    val selected = category == cat
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) CosmicAccent else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (selected) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color Selector
            Text("ЦВЕТ КАРТОЧКИ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicAccent)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoteColors.forEachIndexed { idx, color ->
                    val selected = selectedColorIndex == idx
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorIndex = idx }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(title, content, category, NoteColors[selectedColorIndex].toHex())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_note_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorBottomSheet(
    note: NoteEntity,
    viewModel: AssistantViewModel,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var category by remember { mutableStateOf(note.category) }
    var noteColorHex by remember { mutableStateOf(note.colorHex) }

    // AI dynamic operation result dialog
    val aiOperationResult by viewModel.aiOperationResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val contentResolver = LocalContext.current.contentResolver

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clearAiOperationResult()
            onDismiss()
        },
        containerColor = CosmicSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ИЗМЕНИТЬ ЗАМЕТКУ",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Текст заметки") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Smart operations powered by Gemini AI
            Text(
                text = "СМАРТ-ИНТЕГРАЦИЯ С GEMINI ИИ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val operationButtons = listOf(
                    Triple("Сократить", "summarize", Icons.Filled.ClearAll),
                    Triple("Развернуть", "expand", Icons.Filled.AutoAwesome),
                    Triple("Дела (To-Do)", "todo", Icons.Filled.TaskAlt),
                    Triple("Креатив", "ideas", Icons.Filled.Lightbulb)
                )

                operationButtons.forEach { (label, act, icon) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isAiLoading && content.isNotBlank()) {
                                viewModel.processNoteWithAi(
                                    note = note.copy(title = title, content = content),
                                    actionType = act
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = CosmicAccent.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CosmicAccent.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // AI loading / reply area
            AnimatedVisibility(
                visible = isAiLoading || aiOperationResult != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAiLoading) "Генерация ответа ИИ..." else "Предложение от Gemini Aura:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB300)
                            )

                            if (aiOperationResult != null) {
                                Row {
                                    TextButton(
                                        onClick = {
                                            aiOperationResult?.let { reply ->
                                                content = "$content\n\n=== ИИ Дополнение ===\n$reply"
                                                viewModel.clearAiOperationResult()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Добавить", fontSize = 10.sp, bold = true)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    TextButton(
                                        onClick = {
                                            aiOperationResult?.let { reply ->
                                                content = reply
                                                viewModel.clearAiOperationResult()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Заменить", fontSize = 10.sp, bold = true)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isAiLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                            }
                        } else {
                            aiOperationResult?.let { result ->
                                Text(
                                    text = result,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    maxHeight = 200.dp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cat + Color Selectors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("КАТЕГОРИЯ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val cats = listOf("Личное", "Работа", "Идеи", "Дела")
                        // Display small indicator or dropdown
                        // Let's draw horizontal categories with scroll
                        cats.forEach { cat ->
                            val selected = category == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selected) CosmicAccent else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { category = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(cat, fontSize = 9.sp, color = if (selected) Color.White else Color.LightGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        note.copy(
                            title = title,
                            content = content,
                            category = category,
                            colorHex = noteColorHex
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить изменения", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// Text Helper
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    textAlign: TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    maxHeight: androidx.compose.ui.unit.Dp? = null
) {
    if (maxHeight != null) {
        Box(modifier = Modifier.height(maxHeight)) {
            LazyColumn {
                item {
                    androidx.compose.material3.Text(
                        text = text,
                        modifier = modifier,
                        color = color,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        textAlign = textAlign,
                        lineHeight = lineHeight,
                        maxLines = maxLines,
                        overflow = overflow,
                        style = style
                    )
                }
            }
        }
    } else {
        androidx.compose.material3.Text(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textAlign = textAlign,
            lineHeight = lineHeight,
            maxLines = maxLines,
            overflow = overflow,
            style = style
        )
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.textButtonColors(contentColor = CosmicAccent),
        content = content
    )
}

fun TextSpan(text: String, bold: Boolean = false): @Composable () -> Unit {
    return {
        Text(text = text, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ComicAccentBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(CosmicAccent, Color(0xFF9055FF))
    )
}

@Composable
fun SolidGreyBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f))
    )
}

// Extra utility
@Composable
fun Text(text: String, fontSize: androidx.compose.ui.unit.TextUnit, bold: Boolean) {
    Text(text = text, fontSize = fontSize, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
}
