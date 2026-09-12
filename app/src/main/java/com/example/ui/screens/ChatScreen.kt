package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.MyraCardBorder
import com.example.ui.theme.MyraCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isChatAiThinking.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .testTag("chat_screen")
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MYRA Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "हिंदी AI Assistant • अमित",
                        fontSize = 11.sp,
                        color = NeonCyan
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearChatHistory() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Clear Chat",
                    tint = TextMuted
                )
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    EmptyChatWelcome(onSelectPrompt = { prompt ->
                        viewModel.processCommand(prompt)
                    })
                }
            } else {
                items(chatMessages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onSpeak = { text -> viewModel.speakText(text) }
                    )
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MYRA सोच रही है...",
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field and Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("अमित, यहाँ संदेश लिखें...", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MyraCardBorder,
                    focusedContainerColor = MyraCardSurface,
                    unfocusedContainerColor = MyraCardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic or Send Button
            if (inputText.isNotBlank()) {
                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        inputText = ""
                        viewModel.processCommand(text)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            } else {
                IconButton(
                    onClick = { viewModel.toggleVoiceListening() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MyraCardSurface)
                        .border(1.dp, NeonCyan, CircleShape)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Input",
                        tint = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: (String) -> Unit
) {
    val isUser = message.sender == "USER"
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f)))
                    else Brush.linearGradient(listOf(MyraCardSurface, MyraCardSurface))
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) NeonCyan.copy(alpha = 0.4f) else MyraCardBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = TextMuted
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = { onSpeak(message.message) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatWelcome(
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "नमस्ते अमित! मैं आपकी क्या मदद करूँ?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "यहाँ आप सीधे लिख सकते हैं या बोलकर निर्देश दे सकते हैं।",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))

        val suggestions = listOf(
            "आप क्या-क्या कर सकती हैं?",
            "YouTube पर Hindi Songs सर्च करो",
            "आज की ताज़ा खबरें क्या हैं?",
            "एक बढ़िया सा चुटकुला सुनाओ",
            "मेरे फोन की बैटरी स्थिति बताओ"
        )

        suggestions.forEach { prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MyraCardSurface)
                    .border(1.dp, MyraCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelectPrompt(prompt) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "💬 \"$prompt\"",
                    fontSize = 13.sp,
                    color = NeonCyan
                )
            }
        }
    }
}
