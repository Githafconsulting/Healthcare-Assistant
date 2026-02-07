package com.afya.assistant.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

data class Message(
    val id: String,
    val role: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long
)

@Composable
fun VoiceAssistantScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var textToSpeech: TextToSpeech? = remember { null }

    // Initialize TTS
    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                Log.d("VoiceAssistant", "TTS initialized")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF0EA5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Assistant",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
                Column {
                    Text(
                        "Afya Voice Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Clinical AI Support",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(32.dp)
                        ) {
                            Text(
                                "Start a Conversation",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ask for clinical guidance,\ntreatment recommendations,\nor emergency protocols",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }

            items(messages) { message ->
                MessageBubble(message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFF0EA5E9),
                            shape = RoundedCornerShape(50)
                        ) {
                            // Placeholder
                        }
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                "Processing...",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Input Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mic Button
                    IconButton(
                        onClick = {
                            if (isListening) {
                                speechRecognizer.stopListening()
                                isListening = false
                            } else {
                                startListening(context, speechRecognizer) { text ->
                                    inputText += text
                                }
                                isListening = true
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isListening) Color(0xFFEF4444) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Listen",
                            tint = if (isListening) Color.White else Color(0xFF4B5563)
                        )
                    }

                    // Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask a question...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0EA5E9),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    sendMessage(
                                        inputText,
                                        context,
                                        onResponse = { response ->
                                            messages = messages + listOf(
                                                Message(
                                                    id = System.currentTimeMillis().toString(),
                                                    role = "assistant",
                                                    text = response,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            )
                                            // Speak response
                                            textToSpeech?.speak(
                                                response.take(200),
                                                TextToSpeech.QUEUE_FLUSH,
                                                null
                                            )
                                            isLoading = false
                                        },
                                        onError = {
                                            isLoading = false
                                        }
                                    )
                                    messages = messages + listOf(
                                        Message(
                                            id = System.currentTimeMillis().toString(),
                                            role = "user",
                                            text = inputText,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    inputText = ""
                                    isLoading = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF0EA5E9),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }

                if (isListening) {
                    Text(
                        "Listening...",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (message.role == "user") {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (message.role == "user") Color(0xFF0EA5E9) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            color = if (message.role == "user") Color(0xFF0EA5E9) else Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = if (message.role == "assistant") 2.dp else 0.dp
        ) {
            Text(
                message.text,
                fontSize = 14.sp,
                color = if (message.role == "user") Color.White else Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun startListening(
    context: Context,
    speechRecognizer: SpeechRecognizer,
    onResult: (String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    }
    speechRecognizer.startListening(intent)
}

private suspend fun sendMessage(
    message: String,
    context: Context,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("http://your-api-server:5000/api/chat")
            val connection = url.openConnection()
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val requestBody = JSONObject().apply {
                put("message", message)
                put("context", "clinical-decision-support")
            }

            connection.outputStream.write(requestBody.toString().toByteArray())

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            val assistantMessage = jsonResponse.getString("message")

            withContext(Dispatchers.Main) {
                onResponse(assistantMessage)
            }
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Error sending message", e)
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
