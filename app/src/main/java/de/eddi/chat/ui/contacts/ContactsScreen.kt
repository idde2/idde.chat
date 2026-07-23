package de.eddi.chat.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.eddi.chat.data.Contact
import de.eddi.chat.data.NetworkModule
import de.eddi.chat.data.TokenStorage
import de.eddi.chat.theme.Purple
import de.eddi.chat.theme.PurpleLight
import de.eddi.chat.ui.components.GlowCard
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    tokenStorage: TokenStorage,
    onOpenChat: (Int, String) -> Unit,
    onAddContact: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    var myId by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        val token = tokenStorage.tokenFlow.firstOrNull()
        myId = tokenStorage.userIdFlow.firstOrNull() ?: -1
        if (token == null) {
            onLogout()
            return@LaunchedEffect
        }
        try {
            val response = NetworkModule.apiService.getContacts("Bearer $token")
            if (response.isSuccessful) {
                contacts = response.body()?.contacts ?: emptyList()
            } else if (response.code() == 401) {
                tokenStorage.clearAuth()
                onLogout()
            } else {
                errorMsg = "Fehler beim Laden der Kontakte"
            }
        } catch (e: Exception) {
            errorMsg = "Verbindungsfehler: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kontakte",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            tokenStorage.clearAuth()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddContact(myId) },
                containerColor = Purple,
                contentColor   = androidx.compose.ui.graphics.Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Kontakt hinzufügen")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple
                )
                errorMsg.isNotEmpty() -> Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                contacts.isEmpty() -> Text(
                    text = "Noch keine Kontakte.\nKlicke auf +, um jemanden zu finden!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contacts) { contact ->
                        ContactItem(contact = contact) {
                            onOpenChat(contact.id, contact.username)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = PurpleLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = contact.username,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Klicke zum Chatten",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
