package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogFood
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.ui.theme.CarbsOchre
import com.example.ui.theme.FatTeal
import com.example.ui.theme.LimeSpark
import com.example.ui.theme.OverGoalAmber
import com.example.ui.theme.ProteinCoral
import com.example.ui.theme.WaterCyan
import com.example.ui.viewmodel.DailySummary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun CalorieRingHeroCard(
  summary: DailySummary,
  profile: UserProfile,
  modifier: Modifier = Modifier,
) {
  val goal = profile.dailyCalorieGoal.coerceAtLeast(1L)
  val consumed = summary.caloriesConsumed
  val remaining = goal - consumed
  val isOver = remaining < 0
  val progressRaw = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1.15f)
  val animatedProgress by animateFloatAsState(
    targetValue = progressRaw.coerceAtMost(1f),
    animationSpec = spring(stiffness = 220f),
    label = "calorie_ring_progress",
  )

  val ringTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
  val ringActiveColor = if (isOver) OverGoalAmber else LimeSpark

  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
              MaterialTheme.colorScheme.secondary,
            )
          )
        )
        .padding(20.dp),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(144.dp),
          ) {
            Canvas(modifier = Modifier.size(134.dp)) {
              val strokeWidth = 14.dp.toPx()
              val diameter = size.minDimension - strokeWidth
              val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
              val arcSize = Size(diameter, diameter)

              drawArc(
                color = ringTrackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
              )
              drawArc(
                color = ringActiveColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (isOver) "${abs(remaining)}" else "$remaining",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary,
              )
              Text(
                text = if (isOver) "kcal acima" else "kcal restantes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
              )
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            HeroStatItem(
              label = "Meta diária",
              value = "$goal kcal",
              accent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
            HeroStatItem(
              label = "Consumidas",
              value = "$consumed kcal",
              accent = ringActiveColor,
            )
            val pct = ((consumed.toDouble() / goal.toDouble()) * 100.0).roundToInt()
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
            ) {
              Text(
                text = "$pct% da meta diária",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 14.dp),
          ) {
            MacroProgressColumn(
              label = "Proteínas",
              currentG = summary.proteinG,
              goalG = profile.proteinGoalG,
              barColor = ProteinCoral,
              modifier = Modifier.weight(1f),
            )
            MacroProgressColumn(
              label = "Carboidratos",
              currentG = summary.carbsG,
              goalG = profile.carbsGoalG,
              barColor = CarbsOchre,
              modifier = Modifier.weight(1f),
            )
            MacroProgressColumn(
              label = "Gorduras",
              currentG = summary.fatG,
              goalG = profile.fatGoalG,
              barColor = FatTeal,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HeroStatItem(
  label: String,
  value: String,
  accent: Color,
) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(accent)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onPrimary,
    )
  }
}

@Composable
private fun MacroProgressColumn(
  label: String,
  currentG: Double,
  goalG: Long,
  barColor: Color,
  modifier: Modifier = Modifier,
) {
  val safeGoal = goalG.coerceAtLeast(1L)
  val progress by animateFloatAsState(
    targetValue = (currentG.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f),
    animationSpec = spring(stiffness = 260f),
    label = "macro_progress_$label",
  )

  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "${currentG.roundToInt()}g / ${safeGoal}g",
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
    )
    Spacer(modifier = Modifier.height(6.dp))
    LinearProgressIndicator(
      progress = { progress },
      color = barColor,
      trackColor = barColor.copy(alpha = 0.18f),
      modifier = Modifier
        .fillMaxWidth()
        .height(7.dp)
        .clip(RoundedCornerShape(99.dp)),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterTrackerCard(
  waterMl: Long,
  goalMl: Long,
  onAdjustWater: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val safeGoal = goalMl.coerceAtLeast(500L)
  val progress by animateFloatAsState(
    targetValue = (waterMl.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f),
    animationSpec = spring(stiffness = 240f),
    label = "water_progress",
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(WaterCyan.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.WaterDrop,
              contentDescription = "Hidratação",
              tint = WaterCyan,
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Hidratação",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "$waterMl ml de $safeGoal ml",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        FilledTonalIconButton(
          onClick = { onAdjustWater(-200L) },
          enabled = waterMl > 0L,
          colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
          ),
          modifier = Modifier
            .size(48.dp)
            .testTag("water_minus_200_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = "Remover 200 ml de água",
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LinearProgressIndicator(
        progress = { progress },
        color = WaterCyan,
        trackColor = WaterCyan.copy(alpha = 0.14f),
        modifier = Modifier
          .fillMaxWidth()
          .height(9.dp)
          .clip(RoundedCornerShape(99.dp)),
      )

      Spacer(modifier = Modifier.height(12.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        WaterQuickButton("+200 ml", "water_plus_200") { onAdjustWater(200L) }
        WaterQuickButton("+350 ml", "water_plus_350") { onAdjustWater(350L) }
        WaterQuickButton("+500 ml", "water_plus_500") { onAdjustWater(500L) }
      }
    }
  }
}

@Composable
private fun WaterQuickButton(
  label: String,
  tag: String,
  onClick: () -> Unit,
) {
  FilledTonalButton(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.filledTonalButtonColors(
      containerColor = WaterCyan.copy(alpha = 0.12f),
      contentColor = WaterCyan,
    ),
    modifier = Modifier
      .height(48.dp)
      .testTag(tag),
  ) {
    Icon(
      imageVector = Icons.Filled.Add,
      contentDescription = null,
      modifier = Modifier.size(16.dp),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = label, style = MaterialTheme.typography.labelLarge)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFoodToMealDialog(
  food: CatalogFood,
  initialMealType: MealType,
  onDismiss: () -> Unit,
  onConfirm: (MealType, Double) -> Unit,
) {
  var selectedMeal by remember { mutableStateOf(initialMealType) }
  var servings by remember { mutableDoubleStateOf(1.0) }

  val totalCalories = (food.calories * servings).roundToLong()
  val totalProtein = (food.proteinG * servings)
  val totalCarbs = (food.carbsG * servings)
  val totalFat = (food.fatG * servings)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = food.name,
          style = MaterialTheme.typography.titleLarge,
        )
        Text(
          text = "Porção base: ${food.servingLabel} (${food.calories} kcal)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        Text(
          text = "Refeição",
          style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          MealType.entries.forEach { meal ->
            FilterChip(
              selected = selectedMeal == meal,
              onClick = { selectedMeal = meal },
              label = { Text(meal.labelPt) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("meal_chip_${meal.key}"),
            )
          }
        }

        Text(
          text = "Quantidade de porções",
          style = MaterialTheme.typography.labelLarge,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          FilledTonalIconButton(
            onClick = { servings = (servings - 0.25).coerceAtLeast(0.25) },
            modifier = Modifier
              .size(48.dp)
              .testTag("servings_minus_button"),
          ) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir porção")
          }
          Text(
            text = String.format(Locale.US, "%.2f x", servings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f),
          )
          FilledTonalIconButton(
            onClick = { servings = (servings + 0.25).coerceAtMost(20.0) },
            modifier = Modifier
              .size(48.dp)
              .testTag("servings_plus_button"),
          ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar porção")
          }
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(0.5, 1.0, 1.5, 2.0).forEach { preset ->
            FilterChip(
              selected = abs(servings - preset) < 0.01,
              onClick = { servings = preset },
              label = { Text("${preset}x") },
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "$totalCalories kcal no total",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "P: ${String.format(Locale.US, "%.1f", totalProtein)}g  •  " +
                "C: ${String.format(Locale.US, "%.1f", totalCarbs)}g  •  " +
                "G: ${String.format(Locale.US, "%.1f", totalFat)}g",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(selectedMeal, servings) },
        modifier = Modifier.testTag("confirm_add_food_button"),
      ) {
        Text("Adicionar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMealEntryDialog(
  entry: MealEntry,
  onDismiss: () -> Unit,
  onSave: (Double, MealType) -> Unit,
  onDelete: () -> Unit,
) {
  var selectedMeal by remember { mutableStateOf(MealType.fromKey(entry.mealType)) }
  var servings by remember { mutableDoubleStateOf(entry.servings.coerceAtLeast(0.25)) }

  val ratio = servings / entry.servings.coerceAtLeast(0.1)
  val previewKcal = (entry.calories * ratio).roundToLong()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = entry.foodName, style = MaterialTheme.typography.titleLarge)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Porção: ${entry.servingLabel}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          MealType.entries.forEach { meal ->
            FilterChip(
              selected = selectedMeal == meal,
              onClick = { selectedMeal = meal },
              label = { Text(meal.labelPt) },
            )
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          FilledTonalIconButton(
            onClick = { servings = (servings - 0.25).coerceAtLeast(0.25) },
            modifier = Modifier.size(48.dp),
          ) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir")
          }
          Text(
            text = "${String.format(Locale.US, "%.2f", servings)}x ($previewKcal kcal)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
          )
          FilledTonalIconButton(
            onClick = { servings = (servings + 0.25).coerceAtMost(20.0) },
            modifier = Modifier.size(48.dp),
          ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar")
          }
        }
        OutlinedButton(
          onClick = onDelete,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("delete_meal_entry_button"),
        ) {
          Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Excluir registro")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(servings, selectedMeal) },
        modifier = Modifier.testTag("save_meal_entry_button"),
      ) {
        Text("Salvar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Fechar")
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCalorieDialog(
  initialMealType: MealType,
  onDismiss: () -> Unit,
  onConfirm: (String, MealType, Long, Double, Double, Double) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var caloriesText by remember { mutableStateOf("") }
  var proteinText by remember { mutableStateOf("0") }
  var carbsText by remember { mutableStateOf("0") }
  var fatText by remember { mutableStateOf("0") }
  var mealType by remember { mutableStateOf(initialMealType) }

  val caloriesVal = caloriesText.toLongOrNull() ?: 0L

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Entrada rápida", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Descrição (ex: Almoço fora)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_name_input"),
        )
        OutlinedTextField(
          value = caloriesText,
          onValueChange = { caloriesText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Calorias (kcal) *") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_calories_input"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = proteinText,
            onValueChange = { proteinText = it },
            label = { Text("Prot (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = carbsText,
            onValueChange = { carbsText = it },
            label = { Text("Carb (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = fatText,
            onValueChange = { fatText = it },
            label = { Text("Gord (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          MealType.entries.forEach { meal ->
            FilterChip(
              selected = mealType == meal,
              onClick = { mealType = meal },
              label = { Text(meal.labelPt) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            name.ifBlank { "Registro rápido" },
            mealType,
            caloriesVal,
            proteinText.replace(',', '.').toDoubleOrNull() ?: 0.0,
            carbsText.replace(',', '.').toDoubleOrNull() ?: 0.0,
            fatText.replace(',', '.').toDoubleOrNull() ?: 0.0,
          )
        },
        enabled = caloriesVal > 0L,
        modifier = Modifier.testTag("confirm_quick_entry_button"),
      ) {
        Text("Registrar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCustomFoodDialog(
  onDismiss: () -> Unit,
  onCreate: (String, String, Long, Double, Double, Double, MealType?) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var serving by remember { mutableStateOf("100 g") }
  var caloriesText by remember { mutableStateOf("") }
  var proteinText by remember { mutableStateOf("0") }
  var carbsText by remember { mutableStateOf("0") }
  var fatText by remember { mutableStateOf("0") }
  var selectedMeal by remember { mutableStateOf<MealType?>(MealType.LUNCH) }

  val caloriesVal = caloriesText.toLongOrNull() ?: 0L

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Novo alimento", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nome do alimento *") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_food_name_input"),
        )
        OutlinedTextField(
          value = serving,
          onValueChange = { serving = it },
          label = { Text("Porção padrão (ex: 100 g, 1 fatia)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = caloriesText,
          onValueChange = { caloriesText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Calorias por porção (kcal) *") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_food_calories_input"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = proteinText,
            onValueChange = { proteinText = it },
            label = { Text("Prot (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = carbsText,
            onValueChange = { carbsText = it },
            label = { Text("Carb (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = fatText,
            onValueChange = { fatText = it },
            label = { Text("Gord (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
          )
        }
        Text(
          text = "Também adicionar hoje em:",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FilterChip(
            selected = selectedMeal == null,
            onClick = { selectedMeal = null },
            label = { Text("Só salvar") },
          )
          MealType.entries.forEach { meal ->
            FilterChip(
              selected = selectedMeal == meal,
              onClick = { selectedMeal = meal },
              label = { Text(meal.labelPt) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onCreate(
            name.trim(),
            serving.ifBlank { "1 porção" },
            caloriesVal,
            proteinText.replace(',', '.').toDoubleOrNull() ?: 0.0,
            carbsText.replace(',', '.').toDoubleOrNull() ?: 0.0,
            fatText.replace(',', '.').toDoubleOrNull() ?: 0.0,
            selectedMeal,
          )
        },
        enabled = name.isNotBlank() && caloriesVal >= 0L && caloriesText.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_custom_food_button"),
      ) {
        Text("Salvar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    },
  )
}
