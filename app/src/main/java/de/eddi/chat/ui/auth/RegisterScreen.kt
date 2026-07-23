package de.eddi.chat.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.eddi.chat.data.NetworkModule
import de.eddi.chat.data.RegisterRequest
import de.eddi.chat.data.TokenStorage
import de.eddi.chat.theme.DarkBg
import de.eddi.chat.theme.DarkCard
import de.eddi.chat.theme.PurpleLight
import de.eddi.chat.ui.components.ChatTextField
import de.eddi.chat.ui.components.ErrorText
import de.eddi.chat.ui.components.GlowCard
import de.eddi.chat.ui.components.GradientButton
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    tokenStorage: TokenStorage
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(DarkCard, DarkBg),
                    radius = 1400f
                )
            )
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
            // Logo
            Text(
                buildAnnotatedString {
                    append("eddi.")
                    withStyle(SpanStyle(color = PurpleLight)) { append("chat") }
                },
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                "Neues Konto erstellen",
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
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        label = "E-Mail",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
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
                        text = "Registrieren",
                        isLoading = isLoading,
                        onClick = {
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                errorMsg = "Bitte alle Felder ausfüllen"
                                return@GradientButton
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    val resp = NetworkModule.apiService.register(RegisterRequest(username, password, email))
                                    val body = resp.body()
                                    val isSuccess = resp.isSuccessful || resp.code() == 201
                                    if (isSuccess && body?.token != null && body.user != null) {
                                        tokenStorage.saveAuth(body.token, body.user.username, body.user.id)
                                        onRegisterSuccess()
                                    } else {
                                        errorMsg = body?.error ?: "Registrierung fehlgeschlagen (${resp.code()})"
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

            // Zu Login
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    buildAnnotatedString {
                        append("Bereits ein Konto? ")
                        withStyle(SpanStyle(color = PurpleLight, fontWeight = FontWeight.Bold)) {
                            append("Anmelden")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
