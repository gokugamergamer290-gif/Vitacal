package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VitaCalDarkColorScheme = darkColorScheme(
  primary = LuminousSagePrimary,
  onPrimary = DeepPineOnPrimary,
  primaryContainer = PinePrimaryContainer,
  onPrimaryContainer = MintOnPrimaryContainer,
  secondary = ForestSecondary,
  onSecondary = ForestOnSecondary,
  secondaryContainer = ForestSecondaryContainer,
  onSecondaryContainer = ForestOnSecondaryContainer,
  tertiary = LimeTertiary,
  onTertiary = LimeOnTertiary,
  tertiaryContainer = LimeTertiaryContainer,
  onTertiaryContainer = LimeOnTertiaryContainer,
  background = ObsidianForestBackground,
  onBackground = CreamOnBackground,
  surface = PineSurface,
  onSurface = CreamOnBackground,
  surfaceVariant = ElevatedPineSurfaceVariant,
  onSurfaceVariant = SoftSageOnSurfaceVariant,
  outline = PineOutline,
  outlineVariant = PineOutline,
  error = ErrorCrimson,
)

private val VitaCalLightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = EmeraldOnPrimary,
  primaryContainer = SagePrimaryContainer,
  onPrimaryContainer = EmeraldOnPrimaryContainer,
  secondary = LeafSecondary,
  onSecondary = LeafOnSecondary,
  secondaryContainer = MistSecondaryContainer,
  onSecondaryContainer = LeafOnSecondaryContainer,
  tertiary = CitrusTertiary,
  onTertiary = CitrusOnTertiary,
  tertiaryContainer = CitrusTertiaryContainer,
  onTertiaryContainer = CitrusOnTertiaryContainer,
  background = AlabasterBackground,
  onBackground = InkOnBackground,
  surface = CardSurface,
  onSurface = InkOnBackground,
  surfaceVariant = SageSurfaceVariant,
  onSurfaceVariant = MutedOnSurfaceVariant,
  outline = DelicateOutline,
  outlineVariant = DelicateOutline,
  error = ErrorCrimson,
)

@Composable
fun VitaCalTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) VitaCalDarkColorScheme else VitaCalLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = VitaCalTypography,
    content = content,
  )
}
