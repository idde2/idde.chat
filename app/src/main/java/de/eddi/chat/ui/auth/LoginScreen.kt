package de.eddi.chat.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.eddi.chat.data.LoginRequest
import de.eddi.chat.data.NetworkModule
import de.eddi.chat.data.TokenStorage
import de.eddi.chat.theme.Purple
import de.eddi.chat.theme.PurpleLight
import de.eddi.chat.ui.components.ChatTextField
import de.eddi.chat.ui.components.ErrorText
import de.eddi.chat.ui.components.GlowCard
import de.eddi.chat.ui.components.GradientButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    tokenStorage: TokenStorage
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var username  by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var errorMsg  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("eddi.")
                    withStyle(SpanStyle(color = PurpleLight)) { append("chat") }
                },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Willkommen zurück",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ChatTextField(
                        value = username,
                        onValueChange = { username = it; errorMsg = "" },
                        label = "Benutzername",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    ChatTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        label = "Passwort",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    ErrorText(errorMsg)

                    GradientButton(
                        text = "Anmelden",
                        isLoading = isLoading,
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMsg = "Bitte alle Felder ausfüllen"
                                return@GradientButton
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    val resp = NetworkModule.apiService.login(LoginRequest(username, password))
                                    val body = resp.body()
                                    if (resp.isSuccessful && body?.token != null && body.user != null) {
                                        tokenStorage.saveAuth(body.token, body.user.username, body.user.id)
                                        onLoginSuccess()
                                    } else {
                                        errorMsg = body?.error ?: "Login fehlgeschlagen"
                                    }
                                } catch (e: Exception) {
                                    errorMsg = "Verbindungsfehler: ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    buildAnnotatedString {
                        append("Noch kein Konto? ")
                        withStyle(SpanStyle(color = PurpleLight, fontWeight = FontWeight.Bold)) {
                            append("Registrieren")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
