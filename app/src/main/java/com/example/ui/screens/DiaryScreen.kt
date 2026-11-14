package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DateUtils
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.ui.components.CalorieRingHeroCard
import com.example.ui.components.WaterTrackerCard
import com.example.ui.viewmodel.DailySummary
import com.example.ui.viewmodel.UiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
  selectedDateKey: String,
  profileState: UiState<UserProfile>,
  entriesState: UiState<List<MealEntry>>,
  summary: DailySummary,
  waterMl: Long,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onGoToToday: () -> Unit,
  onAdjustWater: (Long) -> Unit,
  onAddFoodForMeal: (MealType) -> Unit,
  onQuickEntryClick: (MealType) -> Unit,
  onEntryClick: (MealEntry) -> Unit,
  modifier: Modifier = Modifier,
) {
  val profile = (profileState as? UiState.Success)?.data ?: UserProfile()
  val isToday = selectedDateKey == DateUtils.todayKey()

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
        DateNavigatorHeader(
          dateLabel = DateUtils.formatDisplayDate(selectedDateKey),
          isToday = isToday,
          onPreviousDay = onPreviousDay,
          onNextDay = onNextDay,
          onGoToToday = onGoToToday,
        )
      }

      item {
        CalorieRingHeroCard(
          summary = summary,
          profile = profile,
        )
      }

      item {
        WaterTrackerCard(
          waterMl = waterMl,
          goalMl = profile.waterGoalMl,
          onAdjustWater = onAdjustWater,
        )
      }

      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Refeições do dia",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          TextButton(
            onClick = { onQuickEntryClick(MealType.SNACK) },
            modifier = Modifier.testTag("quick_calorie_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Bolt,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Entrada rápida")
          }
        }
      }

      if (entriesState is UiState.Loading) {
        item {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
          ) {
            CircularProgressIndicator()
          }
        }
      } else if (entriesState is UiState.Error) {
        item {
          Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = entriesState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      } else {
        MealType.entries.forEach { mealType ->
          item(key = mealType.key) {
            MealSectionCard(
              mealType = mealType,
              entries = summary.entriesByMeal[mealType].orEmpty(),
              onAddFood = { onAddFoodForMeal(mealType) },
              onEntryClick = onEntryClick,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DateNavigatorHeader(
  dateLabel: String,
  isToday: Boolean,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onGoToToday: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
      IconButton(
        onClick = onPreviousDay,
        modifier = Modifier
          .size(48.dp)
          .testTag("previous_day_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Dia anterior",
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f),
      ) {
        Text(
          text = dateLabel,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        AnimatedVisibility(visible = !isToday) {
          TextButton(
            onClick = onGoToToday,
            modifier = Modifier.testTag("go_to_today_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Today,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Hoje", style = MaterialTheme.typography.labelMedium)
          }
        }
      }

      IconButton(
        onClick = onNextDay,
        modifier = Modifier
          .size(48.dp)
          .testTag("next_day_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Próximo dia",
        )
      }
    }
  }
}

@Composable
private fun MealSectionCard(
  mealType: MealType,
  entries: List<MealEntry>,
  onAddFood: () -> Unit,
  onEntryClick: (MealEntry) -> Unit,
) {
  val mealCalories = entries.sumOf { it.calories }
  val mealProtein = entries.sumOf { it.proteinG }.roundToInt()
  val mealCarbs = entries.sumOf { it.carbsG }.roundToInt()
  val mealFat = entries.sumOf { it.fatG }.roundToInt()

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("meal_card_${mealType.key}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = mealIcon(mealType),
              contentDescription = mealType.labelPt,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = mealType.labelPt,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = if (entries.isEmpty()) {
                "Nenhum alimento registrado"
              } else {
                "$mealCalories kcal  •  P ${mealProtein}g • C ${mealCarbs}g • G ${mealFat}g"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        FilledTonalIconButton(
          onClick = onAddFood,
          colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
          ),
          modifier = Modifier
            .size(48.dp)
            .testTag("add_food_${mealType.key}_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Adicionar alimento em ${mealType.labelPt}",
          )
        }
      }

      if (entries.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        entries.forEachIndexed { index, entry ->
          MealEntryRow(
            entry = entry,
            onClick = { onEntryClick(entry) },
          )
          if (index < entries.lastIndex) {
            HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
              modifier = Modifier.padding(horizontal = 4.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MealEntryRow(
  entry: MealEntry,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp)
      .testTag("meal_entry_${entry.id}"),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = entry.foodName,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = "${String.format(Locale.US, "%.2g", entry.servings)}x ${entry.servingLabel}  •  " +
          "P ${entry.proteinG.roundToInt()}g C ${entry.carbsG.roundToInt()}g G ${entry.fatG.roundToInt()}g",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
      Text(
        text = "${entry.calories} kcal",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      )
    }
  }
}

private fun mealIcon(mealType: MealType): ImageVector = when (mealType) {
  MealType.BREAKFAST -> Icons.Filled.Coffee
  MealType.LUNCH -> Icons.Filled.LunchDining
  MealType.DINNER -> Icons.Filled.DinnerDining
  MealType.SNACK -> Icons.Filled.Cookie
}
