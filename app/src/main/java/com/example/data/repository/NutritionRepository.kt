package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CustomFood
import com.example.data.model.MealEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NutritionRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("Usuário precisa estar autenticado com o Google.")
  }

  private fun <T> Flow<T>.logFirestoreErrors(
    userId: String,
    operationType: OperationType,
    path: String,
  ): Flow<T> = catch { error ->
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
      handleFirestoreError(exceptionToLog, operationType, path, auth)
    }
    throw error
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    val docRef = db.collection("users").document(userId)
    return docRef.snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }
      .logFirestoreErrors(userId, OperationType.GET, docRef.path)
  }

  suspend fun ensureUserProfile(displayName: String): UserProfile {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        existing.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: UserProfile(userId = uid, displayName = displayName)
      } else {
        val defaultProfile = UserProfile(
          userId = uid,
          displayName = displayName.take(100),
        )
        val payload = mapOf(
          "userId" to uid,
          "displayName" to defaultProfile.displayName,
          "dailyCalorieGoal" to defaultProfile.dailyCalorieGoal,
          "proteinGoalG" to defaultProfile.proteinGoalG,
          "carbsGoalG" to defaultProfile.carbsGoalG,
          "fatGoalG" to defaultProfile.fatGoalG,
          "waterGoalMl" to defaultProfile.waterGoalMl,
          "weightKg" to defaultProfile.weightKg,
          "heightCm" to defaultProfile.heightCm,
          "age" to defaultProfile.age,
          "sex" to defaultProfile.sex,
          "activityLevel" to defaultProfile.activityLevel,
          "goalType" to defaultProfile.goalType,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(payload).await()
        defaultProfile
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      throw e
    }
  }

  suspend fun saveUserProfile(profile: UserProfile) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        val updates = mapOf(
          "displayName" to profile.displayName.take(100),
          "dailyCalorieGoal" to profile.dailyCalorieGoal.coerceIn(500L, 10000L),
          "proteinGoalG" to profile.proteinGoalG.coerceIn(10L, 600L),
          "carbsGoalG" to profile.carbsGoalG.coerceIn(10L, 1000L),
          "fatGoalG" to profile.fatGoalG.coerceIn(10L, 400L),
          "waterGoalMl" to profile.waterGoalMl.coerceIn(500L, 10000L),
          "weightKg" to profile.weightKg.coerceIn(20.0, 400.0),
          "heightCm" to profile.heightCm.coerceIn(80L, 260L),
          "age" to profile.age.coerceIn(10L, 120L),
          "sex" to profile.sex,
          "activityLevel" to profile.activityLevel,
          "goalType" to profile.goalType,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.update(updates).await()
      } else {
        val payload = mapOf(
          "userId" to uid,
          "displayName" to profile.displayName.take(100),
          "dailyCalorieGoal" to profile.dailyCalorieGoal.coerceIn(500L, 10000L),
          "proteinGoalG" to profile.proteinGoalG.coerceIn(10L, 600L),
          "carbsGoalG" to profile.carbsGoalG.coerceIn(10L, 1000L),
          "fatGoalG" to profile.fatGoalG.coerceIn(10L, 400L),
          "waterGoalMl" to profile.waterGoalMl.coerceIn(500L, 10000L),
          "weightKg" to profile.weightKg.coerceIn(20.0, 400.0),
          "heightCm" to profile.heightCm.coerceIn(80L, 260L),
          "age" to profile.age.coerceIn(10L, 120L),
          "sex" to profile.sex,
          "activityLevel" to profile.activityLevel,
          "goalType" to profile.goalType,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  fun observeMealEntriesForDate(userId: String, dateKey: String): Flow<List<MealEntry>> {
    val colRef = db.collection("users").document(userId).collection("meal_entries")
    return colRef.whereEqualTo("dateKey", dateKey)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(MealEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
      }
      .logFirestoreErrors(userId, OperationType.LIST, colRef.path)
  }

  fun observeMealEntriesForDates(userId: String, dateKeys: List<String>): Flow<List<MealEntry>> {
    val colRef = db.collection("users").document(userId).collection("meal_entries")
    return colRef.whereIn("dateKey", dateKeys)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(MealEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }
      .logFirestoreErrors(userId, OperationType.LIST, colRef.path)
  }

  suspend fun addMealEntry(entry: MealEntry): String {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("meal_entries").document()
    val payload = mapOf(
      "userId" to uid,
      "dateKey" to entry.dateKey,
      "mealType" to entry.mealType,
      "foodName" to entry.foodName.trim().take(120),
      "servingLabel" to entry.servingLabel.trim().take(80),
      "servings" to entry.servings.coerceIn(0.05, 100.0),
      "calories" to entry.calories.coerceIn(0L, 10000L),
      "proteinG" to entry.proteinG.coerceIn(0.0, 1000.0),
      "carbsG" to entry.carbsG.coerceIn(0.0, 1000.0),
      "fatG" to entry.fatG.coerceIn(0.0, 1000.0),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun updateMealEntry(entry: MealEntry) {
    val uid = requireUserId()
    require(entry.id.isNotBlank()) { "MealEntry ID must not be blank for update" }
    val docRef = db.collection("users").document(uid).collection("meal_entries").document(entry.id)
    val updates = mapOf(
      "dateKey" to entry.dateKey,
      "mealType" to entry.mealType,
      "foodName" to entry.foodName.trim().take(120),
      "servingLabel" to entry.servingLabel.trim().take(80),
      "servings" to entry.servings.coerceIn(0.05, 100.0),
      "calories" to entry.calories.coerceIn(0L, 10000L),
      "proteinG" to entry.proteinG.coerceIn(0.0, 1000.0),
      "carbsG" to entry.carbsG.coerceIn(0.0, 1000.0),
      "fatG" to entry.fatG.coerceIn(0.0, 1000.0),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun deleteMealEntry(entryId: String) {
    val uid = requireUserId()
    require(entryId.isNotBlank()) { "MealEntry ID must not be blank for delete" }
    val docRef = db.collection("users").document(uid).collection("meal_entries").document(entryId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      throw e
    }
  }

  fun observeWaterLog(userId: String, dateKey: String): Flow<WaterLog?> {
    val docRef = db.collection("users").document(userId).collection("water_logs").document(dateKey)
    return docRef.snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(WaterLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }
      .logFirestoreErrors(userId, OperationType.GET, docRef.path)
  }

  suspend fun setWaterAmount(dateKey: String, amountMl: Long) {
    val uid = requireUserId()
    val clamped = amountMl.coerceIn(0L, 20000L)
    val docRef = db.collection("users").document(uid).collection("water_logs").document(dateKey)
    try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        docRef.update(
          mapOf(
            "amountMl" to clamped,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          mapOf(
            "userId" to uid,
            "dateKey" to dateKey,
            "amountMl" to clamped,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      throw e
    }
  }

  fun observeCustomFoods(userId: String): Flow<List<CustomFood>> {
    val colRef = db.collection("users").document(userId).collection("custom_foods")
    return colRef.snapshots()
      .map { snapshot ->
        snapshot.toObjects(CustomFood::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.name.lowercase() }
      }
      .logFirestoreErrors(userId, OperationType.LIST, colRef.path)
  }

  suspend fun addCustomFood(food: CustomFood): String {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("custom_foods").document()
    val payload = mapOf(
      "userId" to uid,
      "name" to food.name.trim().take(120),
      "category" to food.category.trim().take(40),
      "servingLabel" to food.servingLabel.trim().take(80),
      "calories" to food.calories.coerceIn(0L, 10000L),
      "proteinG" to food.proteinG.coerceIn(0.0, 1000.0),
      "carbsG" to food.carbsG.coerceIn(0.0, 1000.0),
      "fatG" to food.fatG.coerceIn(0.0, 1000.0),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun deleteCustomFood(foodId: String) {
    val uid = requireUserId()
    require(foodId.isNotBlank()) { "CustomFood ID must not be blank for delete" }
    val docRef = db.collection("users").document(uid).collection("custom_foods").document(foodId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      throw e
    }
  }
}
