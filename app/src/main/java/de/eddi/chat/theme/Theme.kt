package de.eddi.chat.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Color Schemes ────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary          = Purple,
    onPrimary        = Color.White,
    primaryContainer = PurpleDark,
    secondary        = Green,
    onSecondary      = Color.Black,
    background       = DarkBg,
    onBackground     = Color.White,
    surface          = DarkSurface,
    onSurface        = Color.White,
    surfaceVariant   = DarkCard,
    onSurfaceVariant = TextGray,
    outline          = DarkBorder,
    error            = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary          = Purple,
    onPrimary        = Color.White,
    primaryContainer = LightCard,
    secondary        = Green,
    onSecondary      = Color.White,
    background       = LightBg,
    onBackground     = Color(0xFF1C1B2E),
    surface          = LightSurface,
    onSurface        = Color(0xFF1C1B2E),
    surfaceVariant   = LightCard,
    onSurfaceVariant = Color(0xFF555370),
    outline          = Purple,
    error            = Color(0xFFB00020)
)

// ── Extra-Theme-Colors (nicht in Material3 enthalten) ────────────────────

data class ChatColors(
    val myMessageBorder: Color,
    val otherMessageBorder: Color,
    val inputGlow: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val timeText: Color,
)

val LocalChatColors = staticCompositionLocalOf {
    ChatColors(
        myMessageBorder    = Green,
        otherMessageBorder = MsgGray,
        inputGlow          = Color(0x80006020),
        gradientStart      = Color(0xFF222222),
        gradientEnd        = Color(0xFF333333),
        timeText           = MsgGray
    )
}

private val DarkChatColors = ChatColors(
    myMessageBorder    = Green,
    otherMessageBorder = MsgGray,
    inputGlow          = Color(0x8000C040),
    gradientStart      = Color(0xFF1A1030),
    gradientEnd        = Color(0xFF0D081E),
    timeText           = MsgGray
)

private val LightChatColors = ChatColors(
    myMessageBorder    = Purple,
    otherMessageBorder = Color(0xFFCCCCDD),
    inputGlow          = Color(0x408A2BE2),
    gradientStart      = Color(0xFFF5F0FF),
    gradientEnd        = Color(0xFFEDE7FF),
    timeText           = Color(0xFF888888)
)

// ── Theme-Composable ─────────────────────────────────────────────────────

@Composable
fun EddiChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val chatColors  = if (darkTheme) DarkChatColors  else LightChatColors

    CompositionLocalProvider(LocalChatColors provides chatColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}

// Convenience accessor
val MaterialTheme.chatColors: ChatColors
    @Composable get() = LocalChatColors.current
