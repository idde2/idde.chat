package de.eddi.chat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.eddi.chat.data.*
import de.eddi.chat.theme.Purple
import de.eddi.chat.theme.chatColors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: Int,
    receiverName: String,
    tokenStorage: TokenStorage,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages  by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg  by remember { mutableStateOf("") }
    var myId      by remember { mutableStateOf(-1) }
    var myToken   by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val tok = tokenStorage.tokenFlow.firstOrNull() ?: ""
        val uid = tokenStorage.userIdFlow.firstOrNull() ?: -1
        myToken = tok
        myId    = uid

        if (tok.isEmpty()) {
            isLoading = false
            onLogout()
            return@LaunchedEffect
        }

        try {
            val resp = NetworkModule.apiService.getMessages("Bearer $tok", receiverId)
            if (resp.isSuccessful) {
                messages = resp.body()?.messages ?: emptyList()
            } else if (resp.code() == 401) {
                tokenStorage.clearAuth()
                onLogout()
                return@LaunchedEffect
            } else {
                errorMsg = "Fehler: ${resp.code()} ${resp.message()}"
            }
        } catch (e: Exception) {
            errorMsg = "Netzwerkfehler: ${e.localizedMessage}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }

        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }

        SocketManager.connect(tok, uid, receiverId)
        SocketManager.setMessageCallback { content, _, senderId ->
            // Update UI thread-safe
            scope.launch {
                val isMine = senderId == uid
                if (senderId == receiverId || isMine) {
                    val newMessage = Message(
                        sender_id   = senderId,
                        receiver_id = if (isMine) receiverId else uid,
                        content     = content,
                        time        = "jetzt",
                        is_mine     = isMine
                    )
                    // Verhindere Duplikate, falls API und Socket gleichzeitig feuern
                    if (messages.none { it.content == content && it.sender_id == senderId && it.time == "jetzt" }) {
                        messages = messages + newMessage
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { SocketManager.disconnect() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(receiverName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("online", fontSize = 12.sp, color = Color(0xFF4CAF50))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    titleContentColor      = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Purple
                    )
                    errorMsg.isNotEmpty() -> Text(
                        text     = errorMsg,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    messages.isEmpty() -> Text(
                        text      = "Noch keine Nachrichten.\nSchreib als Erstes!",
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier  = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    else -> LazyColumn(
                        state           = listState,
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg -> MessageBubble(msg) }
                    }
                }
            }

            Surface(
                color          = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value         = inputText,
                        onValueChange = { inputText = it },
                        placeholder   = { Text("Nachricht...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier      = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = Purple,
                            focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank() && myToken.isNotEmpty()) {
                                val textToSend = inputText
                                inputText = ""
                                scope.launch {
                                    try {
                                        val resp = NetworkModule.apiService.sendMessage(
                                            "Bearer $myToken", receiverId, SendMessageRequest(textToSend)
                                        )
                                        if (resp.isSuccessful) {
                                            messages = messages + Message(
                                                sender_id   = myId,
                                                receiver_id = receiverId,
                                                content     = textToSend,
                                                time        = "jetzt",
                                                is_mine     = true
                                            )
                                            listState.animateScrollToItem(messages.size - 1)
                                        } else if (resp.code() == 401) {
                                            tokenStorage.clearAuth()
                                            onLogout()
                                        }
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        },
                        containerColor = Purple,
                        contentColor   = Color.White,
                        shape          = CircleShape,
                        modifier       = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Senden")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: Message) {
    val alignment   = if (msg.is_mine) Alignment.CenterEnd else Alignment.CenterStart
    val borderColor = if (msg.is_mine) MaterialTheme.chatColors.myMessageBorder else MaterialTheme.chatColors.otherMessageBorder
    val bgColor     = if (msg.is_mine) Purple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val shape       = if (msg.is_mine) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(horizontalAlignment = if (msg.is_mine) Alignment.End else Alignment.Start) {
            Surface(
                color    = bgColor,
                shape    = shape,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .border(1.dp, borderColor.copy(alpha = 0.5f), shape)
            ) {
                Text(
                    text     = msg.content,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 15.sp
                )
            }
            Text(
                text     = msg.time,
                fontSize = 10.sp,
                color    = MaterialTheme.chatColors.timeText,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}
