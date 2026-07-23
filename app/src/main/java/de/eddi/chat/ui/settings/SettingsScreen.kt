package de.eddi.chat.ui.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import de.eddi.chat.theme.Purple
import de.eddi.chat.ui.components.GlowCard

const val NOTIFICATION_CHANNEL_ID = "eddi_chat_messages"
const val NOTIFICATION_CHANNEL_NAME = "Neue Nachrichten"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Benachrichtigungen für neue Chat-Nachrichten"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    onToggleDarkMode: () -> Unit,
    notificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    // Permission-Launcher für Android 13+
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createNotificationChannel(context)
            if (!notificationsEnabled) onToggleNotifications()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Dark Mode ─────────────────────────────────────────────────────
            GlowCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Dark Mode",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Erscheinungsbild anpassen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onToggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor  = Purple,
                            checkedTrackColor  = Purple.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // ── Benachrichtigungen ────────────────────────────────────────────
            GlowCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Benachrichtigungen",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Push-Nachrichten bei neuen Chats",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Android 13+ braucht explizite Permission
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        createNotificationChannel(context)
                                        onToggleNotifications()
                                    } else {
                                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    createNotificationChannel(context)
                                    onToggleNotifications()
                                }
                            } else {
                                onToggleNotifications()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Purple,
                            checkedTrackColor = Purple.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // ── Über eddi.chat ────────────────────────────────────────────────
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Purple)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Über eddi.chat",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Version & Informationen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = { showAboutDialog = true }) {
                        Text("Öffnen", color = Purple)
                    }
                }
            }
        }
    }

    // ── Über-Dialog ───────────────────────────────────────────────────────────
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "eddi.chat",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    HorizontalDivider(color = Purple.copy(alpha = 0.2f))
                    Text(
                        "Version 1.0",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Text(
                        "Ein privater, sicherer Chat-Dienst.\nEntwickelt mit Flask + Jetpack Compose.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) {
                        Text("Schließen", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }
}
