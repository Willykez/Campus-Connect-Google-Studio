package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  onPrimary = Color(0xFF001E6C),
  primaryContainer = DeepBlueDark,
  onPrimaryContainer = Color(0xFFDAE2FF),
  secondary = WarmAmber,
  onSecondary = Color(0xFF452B00),
  secondaryContainer = Color(0xFF633F00),
  onSecondaryContainer = WarmAmberLight,
  tertiary = SuccessGreen,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF065F46),
  onTertiaryContainer = Color(0xFFA7F3D0),
  background = BackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondaryDark,
  surfaceContainerLowest = SurfaceContainerLowestDark,
  surfaceContainerLow = SurfaceContainerLowDark,
  surfaceContainer = SurfaceContainerDark,
  surfaceContainerHigh = SurfaceContainerHighDark,
  surfaceContainerHighest = SurfaceContainerHighestDark,
  outline = OutlineDark,
  outlineVariant = OutlineVariantDark,
  error = DangerRed,
  onError = Color.White,
  errorContainer = Color(0xFF7F1D1D),
  onErrorContainer = DangerRedLight
)

val LightColorScheme = lightColorScheme(
  primary = DeepBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFDDE3FF),
  onPrimaryContainer = Color(0xFF001453),
  secondary = WarmAmberDark,
  onSecondary = Color.White,
  secondaryContainer = WarmAmberLight,
  onSecondaryContainer = Color(0xFF78350F),
  tertiary = SuccessGreen,
  onTertiary = Color.White,
  tertiaryContainer = SuccessGreenLight,
  onTertiaryContainer = Color(0xFF064E3B),
  background = BackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondaryLight,
  surfaceContainerLowest = SurfaceContainerLowestLight,
  surfaceContainerLow = SurfaceContainerLowLight,
  surfaceContainer = SurfaceContainerLight,
  surfaceContainerHigh = SurfaceContainerHighLight,
  surfaceContainerHighest = SurfaceContainerHighestLight,
  outline = OutlineLight,
  outlineVariant = OutlineVariantLight,
  error = DangerRed,
  onError = Color.White,
  errorContainer = DangerRedLight,
  onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun CampusConnectTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
