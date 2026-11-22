package com.example.studyguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudyGuardColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = BgMain,
    primaryContainer = CardBg,
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = BgMain,
    tertiary = GlowCyan,
    onTertiary = BgMain,
    background = BgMain,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = BgSecondary,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = ViolationRed,
    onError = TextPrimary,
    errorContainer = ViolationBgCard,
    onErrorContainer = ViolationTextBright
)

@Composable
fun StudyGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyGuardColorScheme,
        typography = Typography,
        content = content
    )
}
