package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.BrazilianFoodCatalog
import com.example.data.model.CatalogFood
import com.example.data.model.CustomFood
import com.example.data.model.DateUtils
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.repository.NutritionRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class DailySummary(
  val caloriesConsumed: Long = 0L,
  val proteinG: Double = 0.0,
  val carbsG: Double = 0.0,
  val fatG: Double = 0.0,
  val entriesByMeal: Map<MealType, List<MealEntry>> = emptyMap(),
)

data class DayCaloriesPoint(
  val dateKey: String,
  val shortDay: String,
  val calories: Long,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModel(
  private val repository: NutritionRepository,
  private val currentUserId: String,
  private val initialDisplayName: String,
) : ViewModel() {

  private val _selectedDateKey = MutableStateFlow(DateUtils.todayKey())
  val selectedDateKey: StateFlow<String> = _selectedDateKey.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private fun unwrapFirestoreMessage(error: Throwable, fallback: String): String {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val rootError = firestoreError ?: error
    Log.w(TAG, fallback, rootError)
    return rootError.localizedMessage ?: fallback
  }

  val userProfileState: StateFlow<UiState<UserProfile>> =
    repository.observeUserProfile(currentUserId)
      .map<UserProfile?, UiState<UserProfile>> { profile ->
        UiState.Success(
          profile ?: UserProfile(
            userId = currentUserId,
            displayName = initialDisplayName,
          )
        )
      }
      .catch { error ->
        emit(UiState.Error(unwrapFirestoreMessage(error, "Erro ao carregar perfil.")))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val dailyEntriesState: StateFlow<UiState<List<MealEntry>>> =
    _selectedDateKey
      .flatMapLatest { dateKey ->
        repository.observeMealEntriesForDate(currentUserId, dateKey)
          .map<List<MealEntry>, UiState<List<MealEntry>>> { UiState.Success(it) }
          .catch { error ->
            emit(UiState.Error(unwrapFirestoreMessage(error, "Erro ao carregar refeições.")))
          }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val dailySummary: StateFlow<DailySummary> =
    dailyEntriesState
      .map { state ->
        val entries = (state as? UiState.Success)?.data.orEmpty()
        DailySummary(
          caloriesConsumed = entries.sumOf { it.calories },
          proteinG = entries.sumOf { it.proteinG },
          carbsG = entries.sumOf { it.carbsG },
          fatG = entries.sumOf { it.fatG },
          entriesByMeal = entries.groupBy { MealType.fromKey(it.mealType) },
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DailySummary(),
      )

  val waterMlState: StateFlow<Long> =
    _selectedDateKey
      .flatMapLatest { dateKey ->
        repository.observeWaterLog(currentUserId, dateKey)
          .map { it?.amountMl ?: 0L }
          .catch { error ->
            unwrapFirestoreMessage(error, "Erro ao observar água.")
            emit(0L)
          }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = 0L,
      )

  val customFoods: StateFlow<List<CustomFood>> =
    repository.observeCustomFoods(currentUserId)
      .catch { error ->
        unwrapFirestoreMessage(error, "Erro ao observar alimentos personalizados.")
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList(),
      )

  val allCatalogFoods: StateFlow<List<CatalogFood>> =
    customFoods
      .map { customList ->
        val mappedCustom = customList.map { custom ->
          CatalogFood(
            id = custom.id,
            name = custom.name,
            category = custom.category,
            servingLabel = custom.servingLabel,
            calories = custom.calories,
            proteinG = custom.proteinG,
            carbsG = custom.carbsG,
            fatG = custom.fatG,
            isCustom = true,
          )
        }
        mappedCustom + BrazilianFoodCatalog.builtInFoods
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BrazilianFoodCatalog.builtInFoods,
      )

  private val recentSevenDateKeys: List<String> = DateUtils.recentDateKeys(DateUtils.todayKey(), 7)

  val weeklyCaloriesPoints: StateFlow<List<DayCaloriesPoint>> =
    repository.observeMealEntriesForDates(currentUserId, recentSevenDateKeys)
      .map { entries ->
        val byDate = entries.groupBy { it.dateKey }
        recentSevenDateKeys.map { key ->
          DayCaloriesPoint(
            dateKey = key,
            shortDay = DateUtils.formatShortWeekday(key),
            calories = byDate[key]?.sumOf { it.calories } ?: 0L,
          )
        }
      }
      .catch { error ->
        unwrapFirestoreMessage(error, "Erro ao observar histórico semanal.")
        emit(recentSevenDateKeys.map { DayCaloriesPoint(it, DateUtils.formatShortWeekday(it), 0L) })
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = recentSevenDateKeys.map { DayCaloriesPoint(it, DateUtils.formatShortWeekday(it), 0L) },
      )

  init {
    viewModelScope.launch {
      try {
        repository.ensureUserProfile(initialDisplayName.ifBlank { "Atleta VitaCal" })
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.w(TAG, "Failed to ensure user profile", e)
      }
    }
  }

  fun shiftSelectedDate(days: Int) {
    _selectedDateKey.value = DateUtils.shiftDateKey(_selectedDateKey.value, days)
  }

  fun selectToday() {
    _selectedDateKey.value = DateUtils.todayKey()
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun addFoodToMeal(
    food: CatalogFood,
    mealType: MealType,
    servings: Double,
  ) {
    val safeServings = servings.coerceIn(0.1, 50.0)
    val entry = MealEntry(
      userId = currentUserId,
      dateKey = _selectedDateKey.value,
      mealType = mealType.key,
      foodName = food.name,
      servingLabel = food.servingLabel,
      servings = safeServings,
      calories = (food.calories * safeServings).roundToLong(),
      proteinG = ((food.proteinG * safeServings) * 10.0).roundToLong() / 10.0,
      carbsG = ((food.carbsG * safeServings) * 10.0).roundToLong() / 10.0,
      fatG = ((food.fatG * safeServings) * 10.0).roundToLong() / 10.0,
    )
    viewModelScope.launch {
      try {
        repository.addMealEntry(entry)
        _snackbarMessage.value = "${food.name} adicionado em ${mealType.labelPt}"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Não foi possível salvar o alimento."
      }
    }
  }

  fun addQuickEntry(
    foodName: String,
    mealType: MealType,
    calories: Long,
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
  ) {
    val entry = MealEntry(
      userId = currentUserId,
      dateKey = _selectedDateKey.value,
      mealType = mealType.key,
      foodName = foodName.trim().ifBlank { "Registro rápido" },
      servingLabel = "1 porção",
      servings = 1.0,
      calories = calories.coerceIn(0L, 10000L),
      proteinG = proteinG.coerceIn(0.0, 1000.0),
      carbsG = carbsG.coerceIn(0.0, 1000.0),
      fatG = fatG.coerceIn(0.0, 1000.0),
    )
    viewModelScope.launch {
      try {
        repository.addMealEntry(entry)
        _snackbarMessage.value = "${entry.foodName} registrado (${entry.calories} kcal)"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao registrar calorias."
      }
    }
  }

  fun updateMealEntryServings(
    entry: MealEntry,
    newServings: Double,
    newMealType: MealType,
  ) {
    val safeOldServings = entry.servings.coerceAtLeast(0.1)
    val safeNewServings = newServings.coerceIn(0.1, 50.0)
    val ratio = safeNewServings / safeOldServings
    val updated = entry.copy(
      mealType = newMealType.key,
      servings = safeNewServings,
      calories = (entry.calories * ratio).roundToLong(),
      proteinG = ((entry.proteinG * ratio) * 10.0).roundToLong() / 10.0,
      carbsG = ((entry.carbsG * ratio) * 10.0).roundToLong() / 10.0,
      fatG = ((entry.fatG * ratio) * 10.0).roundToLong() / 10.0,
    )
    viewModelScope.launch {
      try {
        repository.updateMealEntry(updated)
        _snackbarMessage.value = "Refeição atualizada"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao atualizar item."
      }
    }
  }

  fun deleteMealEntry(entryId: String) {
    viewModelScope.launch {
      try {
        repository.deleteMealEntry(entryId)
        _snackbarMessage.value = "Alimento removido"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao remover item."
      }
    }
  }

  fun adjustWater(deltaMl: Long) {
    val current = waterMlState.value
    val next = (current + deltaMl).coerceIn(0L, 20000L)
    val dateKey = _selectedDateKey.value
    viewModelScope.launch {
      try {
        repository.setWaterAmount(dateKey, next)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao atualizar hidratação."
      }
    }
  }

  fun createCustomFood(
    name: String,
    servingLabel: String,
    calories: Long,
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
    alsoLogToMeal: MealType? = null,
  ) {
    val cleanName = name.trim()
    if (cleanName.isEmpty()) return
    val cleanServing = servingLabel.trim().ifEmpty { "1 porção" }
    val customFood = CustomFood(
      userId = currentUserId,
      name = cleanName,
      category = "Meus Alimentos",
      servingLabel = cleanServing,
      calories = calories.coerceIn(0L, 10000L),
      proteinG = proteinG.coerceIn(0.0, 1000.0),
      carbsG = carbsG.coerceIn(0.0, 1000.0),
      fatG = fatG.coerceIn(0.0, 1000.0),
    )
    viewModelScope.launch {
      try {
        val id = repository.addCustomFood(customFood)
        if (alsoLogToMeal != null) {
          repository.addMealEntry(
            MealEntry(
              userId = currentUserId,
              dateKey = _selectedDateKey.value,
              mealType = alsoLogToMeal.key,
              foodName = cleanName,
              servingLabel = cleanServing,
              servings = 1.0,
              calories = customFood.calories,
              proteinG = customFood.proteinG,
              carbsG = customFood.carbsG,
              fatG = customFood.fatG,
            )
          )
          _snackbarMessage.value = "$cleanName salvo e adicionado em ${alsoLogToMeal.labelPt}"
        } else {
          _snackbarMessage.value = "$cleanName salvo em Meus Alimentos"
        }
        Log.d(TAG, "Created custom food $id")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao salvar alimento personalizado."
      }
    }
  }

  fun deleteCustomFood(foodId: String) {
    viewModelScope.launch {
      try {
        repository.deleteCustomFood(foodId)
        _snackbarMessage.value = "Alimento personalizado excluído"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao excluir alimento."
      }
    }
  }

  fun saveUserProfile(profile: UserProfile) {
    viewModelScope.launch {
      try {
        repository.saveUserProfile(profile.copy(userId = currentUserId))
        _snackbarMessage.value = "Metas salvas com sucesso"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _snackbarMessage.value = e.localizedMessage ?: "Erro ao salvar metas."
      }
    }
  }

  private companion object {
    const val TAG = "NutritionVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
