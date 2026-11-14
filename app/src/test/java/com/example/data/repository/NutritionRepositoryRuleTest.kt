package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CustomFood
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class NutritionRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureAndSaveUserProfile_authenticatedOwner_persistsAndUpdates() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = NutritionRepository(firestore, auth)

    val initial = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureUserProfile("Alice")
    }
    assertEquals(uid, initial.userId)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveUserProfile(initial.copy(dailyCalorieGoal = 2150L, waterGoalMl = 2800L))
    }

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeUserProfile(uid).first { it != null && it.dailyCalorieGoal == 2150L }
    }
    assertNotNull(observed)
    assertEquals(2800L, observed?.waterGoalMl)
  }

  @Test
  fun mealEntries_ownerCrudAndObserve_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = NutritionRepository(firestore, auth)
    val testDate = "2026-11-14"
    val uniqueName = "Frango-${UUID.randomUUID().toString().take(8)}"

    val entryId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addMealEntry(
        MealEntry(
          userId = uid,
          dateKey = testDate,
          mealType = MealType.LUNCH.key,
          foodName = uniqueName,
          servingLabel = "150 g",
          servings = 1.0,
          calories = 248L,
          proteinG = 46.5,
          carbsG = 0.0,
          fatG = 5.4,
        )
      )
    }
    assertTrue(entryId.isNotBlank())

    val entries = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeMealEntriesForDate(uid, testDate).first { list -> list.any { it.id == entryId } }
    }
    val saved = entries.first { it.id == entryId }
    assertEquals(uniqueName, saved.foodName)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.updateMealEntry(saved.copy(servings = 1.5, calories = 372L))
    }

    val updatedEntries = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeMealEntriesForDate(uid, testDate).first { list ->
        list.any { it.id == entryId && it.calories == 372L }
      }
    }
    assertEquals(372L, updatedEntries.first { it.id == entryId }.calories)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.deleteMealEntry(entryId)
    }
  }

  @Test
  fun waterAndCustomFoods_ownerFlow_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = NutritionRepository(firestore, auth)
    val testDate = "2026-11-14"

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.setWaterAmount(testDate, 1200L)
    }
    val water = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeWaterLog(uid, testDate).first { it != null && it.amountMl == 1200L }
    }
    assertEquals(1200L, water?.amountMl)

    val foodId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addCustomFood(
        CustomFood(
          userId = uid,
          name = "Panqueca fit",
          category = "Meus Alimentos",
          servingLabel = "1 unidade",
          calories = 195L,
          proteinG = 14.0,
          carbsG = 22.0,
          fatG = 5.5,
        )
      )
    }
    val customFoods = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeCustomFoods(uid).first { list -> list.any { it.id == foodId } }
    }
    assertEquals("Panqueca fit", customFoods.first { it.id == foodId }.name)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.deleteCustomFood(foodId)
    }
  }

  @Test
  fun observeMealEntries_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    signInTestUser(BOB_EMAIL)
    val bobRepo = NutritionRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeMealEntriesForDate(aliceUid, "2026-11-14").first()
      }
      fail("Expected cross-user access to fail with PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }
  }

  @Test
  fun observeMealEntries_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val repo = NutritionRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeMealEntriesForDate(aliceUid, "2026-11-14").first()
      }
      fail("Expected unauthenticated access to fail with PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice.vitacal@test.com"
    const val BOB_EMAIL = "bob.vitacal@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
