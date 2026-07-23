package de.eddi.chat.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.eddi.chat.data.NetworkModule
import de.eddi.chat.data.TokenStorage
import de.eddi.chat.data.UserItem
import de.eddi.chat.theme.DarkBg
import de.eddi.chat.theme.DarkCard
import de.eddi.chat.theme.Purple
import de.eddi.chat.theme.PurpleLight
import de.eddi.chat.ui.components.ChatTextField
import de.eddi.chat.ui.components.GlowCard
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    tokenStorage: TokenStorage,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<UserItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        token = tokenStorage.tokenFlow.firstOrNull() ?: ""
        if (token.isEmpty()) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kontakt finden", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(DarkBg, DarkCard)))
                .padding(16.dp)
        ) {
            ChatTextField(
                value = query,
                onValueChange = { 
                    query = it
                    if (it.length >= 2) {
                        scope.launch {
                            isLoading = true
                            try {
                                val resp = NetworkModule.apiService.getUsers("Bearer $token", it)
                                if (resp.isSuccessful) {
                                    users = resp.body()?.users ?: emptyList()
                                } else if (resp.code() == 401) {
                                    tokenStorage.clearAuth()
                                    onLogout()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        users = emptyList()
                    }
                },
                label = "Benutzername suchen...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserSearchItem(user) {
                            scope.launch {
                                try {
                                    val resp = NetworkModule.apiService.addContact("Bearer $token", user.id)
                                    if (resp.isSuccessful) {
                                        onBack()
                                    } else if (resp.code() == 401) {
                                        tokenStorage.clearAuth()
                                        onLogout()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(user: UserItem, onAdd: () -> Unit) {
    GlowCard(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PurpleLight)
            }
            Spacer(Modifier.width(16.dp))
            Text(user.username, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("Hinzufügen", color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
