package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ActivityLevel
import com.example.data.model.GoalType
import com.example.data.model.UserProfile
import com.example.data.model.calculateRecommendedGoals
import com.example.ui.theme.LimeSpark
import com.example.ui.theme.OverGoalAmber
import com.example.ui.viewmodel.DayCaloriesPoint
import com.example.ui.viewmodel.UiState
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
  userEmail: String?,
  profileState: UiState<UserProfile>,
  weeklyPoints: List<DayCaloriesPoint>,
  onSaveProfile: (UserProfile) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val currentProfile = (profileState as? UiState.Success)?.data ?: UserProfile()

  var weightText by remember { mutableStateOf(currentProfile.weightKg.toString()) }
  var heightText by remember { mutableStateOf(currentProfile.heightCm.toString()) }
  var ageText by remember { mutableStateOf(currentProfile.age.toString()) }
  var sex by remember { mutableStateOf(currentProfile.sex) }
  var activityLevel by remember { mutableStateOf(ActivityLevel.fromKey(currentProfile.activityLevel)) }
  var goalType by remember { mutableStateOf(GoalType.fromKey(currentProfile.goalType)) }

  var caloriesGoalText by remember { mutableStateOf(currentProfile.dailyCalorieGoal.toString()) }
  var proteinGoalText by remember { mutableStateOf(currentProfile.proteinGoalG.toString()) }
  var carbsGoalText by remember { mutableStateOf(currentProfile.carbsGoalG.toString()) }
  var fatGoalText by remember { mutableStateOf(currentProfile.fatGoalG.toString()) }
  var waterGoalText by remember { mutableStateOf(currentProfile.waterGoalMl.toString()) }

  LaunchedEffect(currentProfile) {
    weightText = currentProfile.weightKg.toString()
    heightText = currentProfile.heightCm.toString()
    ageText = currentProfile.age.toString()
    sex = currentProfile.sex
    activityLevel = ActivityLevel.fromKey(currentProfile.activityLevel)
    goalType = GoalType.fromKey(currentProfile.goalType)
    caloriesGoalText = currentProfile.dailyCalorieGoal.toString()
    proteinGoalText = currentProfile.proteinGoalG.toString()
    carbsGoalText = currentProfile.carbsGoalG.toString()
    fatGoalText = currentProfile.fatGoalG.toString()
    waterGoalText = currentProfile.waterGoalMl.toString()
  }

  val loggedDays = weeklyPoints.filter { it.calories > 0L }
  val averageCalories = if (loggedDays.isEmpty()) 0 else loggedDays.map { it.calories }.average().roundToInt()
  val daysOnTarget = loggedDays.count {
    val diff = it.calories - currentProfile.dailyCalorieGoal
    diff in -350..150
  }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .widthIn(max = 680.dp)
        .fillMaxSize(),
    ) {
      item {
        Text(
          text = "Progresso e Metas",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      item {
        WeeklyCaloriesChartCard(
          points = weeklyPoints,
          goalCalories = currentProfile.dailyCalorieGoal,
          averageCalories = averageCalories,
          daysOnTarget = daysOnTarget,
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
          ) {
            Text(
              text = "Calculadora metabólica (TMB)",
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              text = "Ajuste seus dados para recalcular calorias e macros pela equação Mifflin-St Jeor.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                label = { Text("Altura (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                label = { Text("Idade") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
            }

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              FilterChip(
                selected = sex == "M",
                onClick = { sex = "M" },
                label = { Text("Masculino") },
              )
              FilterChip(
                selected = sex == "F",
                onClick = { sex = "F" },
                label = { Text("Feminino") },
              )
            }

            Text("Nível de atividade", style = MaterialTheme.typography.labelLarge)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              ActivityLevel.entries.forEach { level ->
                FilterChip(
                  selected = activityLevel == level,
                  onClick = { activityLevel = level },
                  label = { Text(level.labelPt) },
                )
              }
            }

            Text("Objetivo", style = MaterialTheme.typography.labelLarge)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              GoalType.entries.forEach { goal ->
                FilterChip(
                  selected = goalType == goal,
                  onClick = { goalType = goal },
                  label = { Text(goal.labelPt) },
                )
              }
            }

            OutlinedButton(
              onClick = {
                val w = weightText.replace(',', '.').toDoubleOrNull()?.coerceIn(25.0, 350.0) ?: 70.0
                val h = heightText.toLongOrNull()?.coerceIn(90L, 250L) ?: 172L
                val a = ageText.toLongOrNull()?.coerceIn(12L, 110L) ?: 28L
                val rec = calculateRecommendedGoals(w, h, a, sex, activityLevel, goalType)
                caloriesGoalText = rec.calories.toString()
                proteinGoalText = rec.proteinG.toString()
                carbsGoalText = rec.carbsG.toString()
                fatGoalText = rec.fatG.toString()
                waterGoalText = rec.waterMl.toString()
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("calculate_tdee_button"),
            ) {
              Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Calcular metas recomendadas")
            }
          }
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
          ) {
            Text(
              text = "Metas diárias ativas",
              style = MaterialTheme.typography.titleLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = caloriesGoalText,
                onValueChange = { caloriesGoalText = it.filter { c -> c.isDigit() } },
                label = { Text("Calorias (kcal)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                  .weight(1f)
                  .testTag("goal_calories_input"),
              )
              OutlinedTextField(
                value = waterGoalText,
                onValueChange = { waterGoalText = it.filter { c -> c.isDigit() } },
                label = { Text("Água (ml)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = proteinGoalText,
                onValueChange = { proteinGoalText = it.filter { c -> c.isDigit() } },
                label = { Text("Prot (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = carbsGoalText,
                onValueChange = { carbsGoalText = it.filter { c -> c.isDigit() } },
                label = { Text("Carb (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = fatGoalText,
                onValueChange = { fatGoalText = it.filter { c -> c.isDigit() } },
                label = { Text("Gord (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
              )
            }
            Button(
              onClick = {
                val updated = currentProfile.copy(
                  dailyCalorieGoal = (caloriesGoalText.toLongOrNull() ?: 2000L).coerceIn(500L, 10000L),
                  proteinGoalG = (proteinGoalText.toLongOrNull() ?: 140L).coerceIn(10L, 600L),
                  carbsGoalG = (carbsGoalText.toLongOrNull() ?: 220L).coerceIn(10L, 1000L),
                  fatGoalG = (fatGoalText.toLongOrNull() ?: 62L).coerceIn(10L, 400L),
                  waterGoalMl = (waterGoalText.toLongOrNull() ?: 2500L).coerceIn(500L, 10000L),
                  weightKg = (weightText.replace(',', '.').toDoubleOrNull() ?: 70.0).coerceIn(20.0, 400.0),
                  heightCm = (heightText.toLongOrNull() ?: 172L).coerceIn(80L, 260L),
                  age = (ageText.toLongOrNull() ?: 28L).coerceIn(10L, 120L),
                  sex = sex,
                  activityLevel = activityLevel.key,
                  goalType = goalType.key,
                )
                onSaveProfile(updated)
              },
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_goals_button"),
            ) {
              Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Salvar metas na nuvem")
            }
          }
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f),
            ) {
              val initial = (currentProfile.displayName.firstOrNull() ?: 'V').uppercaseChar()
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = initial.toString(),
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = currentProfile.displayName.ifBlank { "Usuário VitaCal" },
                  style = MaterialTheme.typography.titleMedium,
                )
                if (!userEmail.isNullOrBlank()) {
                  Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }

            OutlinedButton(
              onClick = onSignOut,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("sign_out_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(stringResource(R.string.sign_out))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WeeklyCaloriesChartCard(
  points: List<DayCaloriesPoint>,
  goalCalories: Long,
  averageCalories: Int,
  daysOnTarget: Int,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val goalLineColor = MaterialTheme.colorScheme.secondary

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Últimos 7 dias",
        style = MaterialTheme.typography.titleLarge,
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        SummaryBadge(
          label = "Média diária",
          value = "$averageCalories kcal",
          modifier = Modifier.weight(1f),
        )
        SummaryBadge(
          label = "Dias na meta",
          value = "$daysOnTarget / 7",
          modifier = Modifier.weight(1f),
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      val maxVal = max(
        goalCalories.toFloat() * 1.2f,
        (points.maxOfOrNull { it.calories } ?: 0L).toFloat() * 1.15f,
      ).coerceAtLeast(1000f)

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp),
      ) {
        val barCount = points.size.coerceAtLeast(1)
        val slotWidth = size.width / barCount
        val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())

        val goalY = size.height * (1f - (goalCalories.toFloat() / maxVal)).coerceIn(0f, 1f)
        drawLine(
          color = goalLineColor.copy(alpha = 0.65f),
          start = Offset(0f, goalY),
          end = Offset(size.width, goalY),
          strokeWidth = 2.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
        )

        points.forEachIndexed { idx, point ->
          val left = idx * slotWidth + (slotWidth - barWidth) / 2f
          drawRoundRect(
            color = trackColor,
            topLeft = Offset(left, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
          )
          if (point.calories > 0L) {
            val ratio = (point.calories.toFloat() / maxVal).coerceIn(0.03f, 1f)
            val barHeight = size.height * ratio
            val barColor = when {
              point.calories > goalCalories + 150L -> OverGoalAmber
              point.calories >= (goalCalories * 0.75f) -> primaryColor
              else -> LimeSpark
            }
            drawRoundRect(
              color = barColor,
              topLeft = Offset(left, size.height - barHeight),
              size = Size(barWidth, barHeight),
              cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth(),
      ) {
        points.forEach { point ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = point.shortDay,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = if (point.calories > 0L) "${point.calories}" else "—",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryBadge(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}
