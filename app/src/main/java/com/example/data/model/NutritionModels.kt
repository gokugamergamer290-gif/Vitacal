package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

enum class MealType(val key: String, val labelPt: String) {
  BREAKFAST("breakfast", "Café da manhã"),
  LUNCH("lunch", "Almoço"),
  DINNER("dinner", "Jantar"),
  SNACK("snack", "Lanches");

  companion object {
    fun fromKey(key: String): MealType = entries.firstOrNull { it.key == key } ?: SNACK
  }
}

enum class ActivityLevel(val key: String, val labelPt: String, val factor: Double) {
  SEDENTARY("sedentary", "Sedentário", 1.2),
  LIGHT("light", "Leve (1-3x/sem)", 1.375),
  MODERATE("moderate", "Moderado (3-5x/sem)", 1.55),
  ACTIVE("active", "Intenso (6-7x/sem)", 1.725);

  companion object {
    fun fromKey(key: String): ActivityLevel = entries.firstOrNull { it.key == key } ?: MODERATE
  }
}

enum class GoalType(val key: String, val labelPt: String, val calorieDelta: Int) {
  LOSE("lose", "Perder peso", -400),
  MAINTAIN("maintain", "Manter peso", 0),
  GAIN("gain", "Ganhar massa", 300);

  companion object {
    fun fromKey(key: String): GoalType = entries.firstOrNull { it.key == key } ?: MAINTAIN
  }
}

@IgnoreExtraProperties
data class UserProfile(
  val userId: String = "",
  val displayName: String = "",
  val dailyCalorieGoal: Long = 2000L,
  val proteinGoalG: Long = 140L,
  val carbsGoalG: Long = 220L,
  val fatGoalG: Long = 62L,
  val waterGoalMl: Long = 2500L,
  val weightKg: Double = 70.0,
  val heightCm: Long = 172L,
  val age: Long = 28L,
  val sex: String = "M",
  val activityLevel: String = ActivityLevel.MODERATE.key,
  val goalType: String = GoalType.MAINTAIN.key,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class MealEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val dateKey: String = "",
  val mealType: String = MealType.BREAKFAST.key,
  val foodName: String = "",
  val servingLabel: String = "1 porção",
  val servings: Double = 1.0,
  val calories: Long = 0L,
  val proteinG: Double = 0.0,
  val carbsG: Double = 0.0,
  val fatG: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class WaterLog(
  @DocumentId val id: String = "",
  val userId: String = "",
  val dateKey: String = "",
  val amountMl: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CustomFood(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val category: String = "Meus Alimentos",
  val servingLabel: String = "1 porção",
  val calories: Long = 0L,
  val proteinG: Double = 0.0,
  val carbsG: Double = 0.0,
  val fatG: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class CatalogFood(
  val id: String,
  val name: String,
  val category: String,
  val servingLabel: String,
  val calories: Long,
  val proteinG: Double,
  val carbsG: Double,
  val fatG: Double,
  val isCustom: Boolean = false,
)

data class DailyNutritionGoals(
  val calories: Long,
  val proteinG: Long,
  val carbsG: Long,
  val fatG: Long,
  val waterMl: Long,
)

fun calculateRecommendedGoals(
  weightKg: Double,
  heightCm: Long,
  age: Long,
  sex: String,
  activity: ActivityLevel,
  goal: GoalType,
): DailyNutritionGoals {
  // Mifflin-St Jeor Equation
  val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
  val bmr = if (sex == "F") base - 161.0 else base + 5.0
  val tdee = (bmr * activity.factor) + goal.calorieDelta
  val targetCalories = tdee.roundToLong().coerceIn(1200L, 6000L)

  // Macro distribution tailored to goal
  val proteinPerKg = when (goal) {
    GoalType.LOSE -> 2.0
    GoalType.MAINTAIN -> 1.8
    GoalType.GAIN -> 2.0
  }
  val proteinG = (weightKg * proteinPerKg).roundToLong().coerceIn(50L, 400L)
  val fatCalories = targetCalories * 0.27
  val fatG = (fatCalories / 9.0).roundToLong().coerceIn(30L, 250L)
  val remainingCalories = (targetCalories - (proteinG * 4L) - (fatG * 9L)).coerceAtLeast(200L)
  val carbsG = (remainingCalories / 4.0).roundToLong().coerceIn(40L, 800L)
  val waterMl = ((weightKg * 35.0) / 50.0).roundToLong() * 50L

  return DailyNutritionGoals(
    calories = targetCalories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatG = fatG,
    waterMl = waterMl.coerceIn(1500L, 5000L),
  )
}

object DateUtils {
  private val ptBrLocale = Locale.forLanguageTag("pt-BR")

  fun todayKey(): String = formatDateKey(Calendar.getInstance())

  fun formatDateKey(calendar: Calendar): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return fmt.format(calendar.time)
  }

  fun shiftDateKey(dateKey: String, daysOffset: Int): String {
    val cal = parseDateKey(dateKey)
    cal.add(Calendar.DAY_OF_MONTH, daysOffset)
    return formatDateKey(cal)
  }

  fun recentDateKeys(endDateKey: String, count: Int = 7): List<String> {
    val keys = mutableListOf<String>()
    for (offset in (count - 1) downTo 0) {
      keys.add(shiftDateKey(endDateKey, -offset))
    }
    return keys
  }

  fun parseDateKey(dateKey: String): Calendar {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    cal.time = fmt.parse(dateKey) ?: Calendar.getInstance().time
    return cal
  }

  fun formatDisplayDate(dateKey: String): String {
    val today = todayKey()
    val yesterday = shiftDateKey(today, -1)
    val tomorrow = shiftDateKey(today, 1)
    return when (dateKey) {
      today -> "Hoje"
      yesterday -> "Ontem"
      tomorrow -> "Amanhã"
      else -> {
        val cal = parseDateKey(dateKey)
        val fmt = SimpleDateFormat("EEE, dd MMM", ptBrLocale)
        fmt.format(cal.time).replaceFirstChar { it.titlecase(ptBrLocale) }
      }
    }
  }

  fun formatShortWeekday(dateKey: String): String {
    val cal = parseDateKey(dateKey)
    val fmt = SimpleDateFormat("EEE", ptBrLocale)
    return fmt.format(cal.time).take(3).replaceFirstChar { it.titlecase(ptBrLocale) }
  }
}
