package de.eddi.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.eddi.chat.theme.Purple
import de.eddi.chat.theme.PurpleDark
import de.eddi.chat.theme.PurpleLight

// ── Gradient Button ───────────────────────────────────────────────────────

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val gradient = Brush.horizontalGradient(listOf(Purple, PurpleDark))

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(52.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled && !isLoading) gradient else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }
    }
}

// ── Styled TextField ──────────────────────────────────────────────────────

@Composable
fun ChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = PurpleLight) } },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = Purple,
            unfocusedBorderColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor     = PurpleLight,
            cursorColor           = Purple
        )
    )
}

// ── Glowing Card ──────────────────────────────────────────────────────────

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Purple.copy(alpha = 0.4f), Color.Transparent)),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = content
    )
}

// ── Loading Overlay ───────────────────────────────────────────────────────

@Composable
fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Purple)
    }
}

// ── Error Snackbar ────────────────────────────────────────────────────────

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    if (message.isNotEmpty()) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier.padding(top = 4.dp)
        )
    }
}
