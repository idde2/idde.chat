package de.eddi.chat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import de.eddi.chat.data.TokenStorage
import de.eddi.chat.theme.EddiChatTheme
import de.eddi.chat.ui.auth.LoginScreen
import de.eddi.chat.ui.auth.RegisterScreen
import de.eddi.chat.ui.chat.ChatScreen
import de.eddi.chat.ui.contacts.AddContactScreen
import de.eddi.chat.ui.contacts.ContactsScreen
import de.eddi.chat.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val tokenStorage = remember { TokenStorage(context) }

    // Beim Start prüfen ob Token vorhanden → direkt zu Contacts
    val startDest: NavKey = remember {
        val token = runBlocking { tokenStorage.tokenFlow.firstOrNull() }
        if (token.isNullOrEmpty()) Login else Contacts
    }

    // Dark/Light Mode State (für Settings-Screen)
    val systemDark = isSystemInDarkTheme()
    val darkModePref by tokenStorage.darkModeFlow.collectAsState(initial = null)
    val isDark = darkModePref ?: systemDark
    val scope = rememberCoroutineScope()

    EddiChatTheme(darkTheme = isDark) {
        val backStack = rememberNavBackStack(startDest)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {

                entry<Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Contacts)
                        },
                        onNavigateToRegister = { backStack.add(Register) },
                        tokenStorage = tokenStorage
                    )
                }

                entry<Register> {
                    RegisterScreen(
                        onRegisterSuccess = {
                            backStack.clear()
                            backStack.add(Contacts)
                        },
                        onNavigateToLogin = { backStack.removeLastOrNull() },
                        tokenStorage = tokenStorage
                    )
                }

                entry<Contacts> {
                    ContactsScreen(
                        tokenStorage = tokenStorage,
                        onOpenChat = { id, name -> backStack.add(Chat(id, name)) },
                        onAddContact = { myId -> backStack.add(AddContact(myId)) },
                        onOpenSettings = { backStack.add(Settings) },
                        onLogout = {
                            backStack.clear()
                            backStack.add(Login)
                        }
                    )
                }

                entry<Chat> { key ->
                    ChatScreen(
                        receiverId   = key.receiverId,
                        receiverName = key.receiverName,
                        tokenStorage = tokenStorage,
                        onBack = { backStack.removeLastOrNull() },
                        onLogout = {
                            backStack.clear()
                            backStack.add(Login)
                        }
                    )
                }

                entry<AddContact> {
                    AddContactScreen(
                        tokenStorage = tokenStorage,
                        onBack = { backStack.removeLastOrNull() },
                        onLogout = {
                            backStack.clear()
                            backStack.add(Login)
                        }
                    )
                }

                entry<Settings> {
                    val notifsEnabled by tokenStorage.notificationsFlow.collectAsState(initial = true)
                    SettingsScreen(
                        isDark  = isDark,
                        onToggleDarkMode = { scope.launch { tokenStorage.setDarkMode(!isDark) } },
                        notificationsEnabled = notifsEnabled,
                        onToggleNotifications = { scope.launch { tokenStorage.setNotificationsEnabled(!notifsEnabled) } },
                        onBack  = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
