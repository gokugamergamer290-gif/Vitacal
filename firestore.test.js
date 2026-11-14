const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const DATE_KEY = "2026-11-14";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid, ts) {
  return {
    userId: uid,
    displayName: "Alice Silva",
    dailyCalorieGoal: 2000,
    proteinGoalG: 140,
    carbsGoalG: 220,
    fatGoalG: 65,
    waterGoalMl: 2500,
    weightKg: 68.5,
    heightCm: 170,
    age: 29,
    sex: "F",
    activityLevel: "moderate",
    goalType: "maintain",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validMealEntry(uid, ts) {
  return {
    userId: uid,
    dateKey: DATE_KEY,
    mealType: "lunch",
    foodName: "Peito de frango grelhado",
    servingLabel: "150 g",
    servings: 1.0,
    calories: 248,
    proteinG: 46.5,
    carbsG: 0.0,
    fatG: 5.4,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validWaterLog(uid, ts) {
  return {
    userId: uid,
    dateKey: DATE_KEY,
    amountMl: 750,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validCustomFood(uid, ts) {
  return {
    userId: uid,
    name: "Vitamina proteica",
    category: "Bebidas",
    servingLabel: "1 copo (300 ml)",
    calories: 280,
    proteinG: 28.0,
    carbsG: 26.0,
    fatG: 6.5,
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write any user collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 2000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("meal_entries").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("water_logs").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("custom_foods").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("meal_entries").doc("e1").set(validMealEntry(ALICE_UID, ts))
  );
});

test("UserProfile: owner can create, read, and update; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const createdTs = new Date(Date.now() - 5000);
  const updatedTs = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, createdTs))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Valid partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyCalorieGoal: 2200,
      updatedAt: updatedTs,
    })
  );

  // Cross-user read/update blocked
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ dailyCalorieGoal: 1500, updatedAt: updatedTs })
  );

  // Shadow / ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyCalorieGoal: 1900,
      isAdmin: true,
      updatedAt: updatedTs,
    })
  );

  // Out-of-bounds calorie goal rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyCalorieGoal: 100,
      updatedAt: updatedTs,
    })
  );
});

test("MealEntry: owner CRUD succeeds; cross-user and invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const createdTs = new Date(Date.now() - 5000);
  const updatedTs = new Date(Date.now() - 1000);

  const entryRef = aliceDb.collection("users").doc(ALICE_UID).collection("meal_entries").doc("meal_1");
  await assertSucceeds(entryRef.set(validMealEntry(ALICE_UID, createdTs)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("meal_entries").where("dateKey", "==", DATE_KEY).get()
  );

  // Update servings & calories
  await assertSucceeds(
    entryRef.update({
      servings: 1.5,
      calories: 372,
      proteinG: 69.8,
      updatedAt: updatedTs,
    })
  );

  // Cross-user access denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("meal_entries").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("meal_entries").doc("meal_1").delete());

  // Invalid mealType enum rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("meal_entries").doc("meal_bad").set({
      ...validMealEntry(ALICE_UID, createdTs),
      mealType: "midnight_feast",
    })
  );

  // Extra "id" field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("meal_entries").doc("meal_id_leak").set({
      ...validMealEntry(ALICE_UID, createdTs),
      id: "meal_id_leak",
    })
  );

  // Immutable createdAt mutation rejected
  await assertFails(
    entryRef.update({
      createdAt: updatedTs,
      updatedAt: updatedTs,
    })
  );

  await assertSucceeds(entryRef.delete());
});

test("WaterLog: owner create/update/list succeeds; invalid dateKey and negative ml fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const createdTs = new Date(Date.now() - 5000);
  const updatedTs = new Date(Date.now() - 1000);

  const waterRef = aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").doc(DATE_KEY);
  await assertSucceeds(waterRef.set(validWaterLog(ALICE_UID, createdTs)));
  await assertSucceeds(waterRef.update({ amountMl: 1250, updatedAt: updatedTs }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").get());

  // Invalid dateKey document ID fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").doc("invalid_key").set(validWaterLog(ALICE_UID, createdTs))
  );

  // Negative amountMl fails
  await assertFails(waterRef.update({ amountMl: -100, updatedAt: updatedTs }));

  // Cross-user read fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("water_logs").doc(DATE_KEY).get());
});

test("CustomFood: owner CRUD succeeds; cross-user and spoofed userId fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const createdTs = new Date(Date.now() - 5000);
  const updatedTs = new Date(Date.now() - 1000);

  const foodRef = aliceDb.collection("users").doc(ALICE_UID).collection("custom_foods").doc("food_1");
  await assertSucceeds(foodRef.set(validCustomFood(ALICE_UID, createdTs)));
  await assertSucceeds(foodRef.update({ calories: 310, updatedAt: updatedTs }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("custom_foods").get());

  // Spoofed userId inside Bob's own path fails
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).collection("custom_foods").doc("food_spoof").set(validCustomFood(ALICE_UID, createdTs))
  );

  // Cross-user read fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("custom_foods").get());

  await assertSucceeds(foodRef.delete());
});
