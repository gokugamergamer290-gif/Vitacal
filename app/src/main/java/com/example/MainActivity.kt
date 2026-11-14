package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.CatalogFood
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.repository.NutritionRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.AddFoodToMealDialog
import com.example.ui.components.CreateCustomFoodDialog
import com.example.ui.components.EditMealEntryDialog
import com.example.ui.components.QuickCalorieDialog
import com.example.ui.screens.DiaryScreen
import com.example.ui.screens.FoodsScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.theme.VitaCalTheme
import com.example.ui.viewmodel.NutritionViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_DIARY = "diary"
const val ROUTE_FOODS = "foods"
const val ROUTE_PROGRESS = "progress"

private data class NavTab(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val NAV_TABS = listOf(
  NavTab(ROUTE_DIARY, R.string.nav_diary, Icons.Filled.Today, Icons.Outlined.Today),
  NavTab(ROUTE_FOODS, R.string.nav_foods, Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu),
  NavTab(ROUTE_PROGRESS, R.string.nav_progress, Icons.Filled.BarChart, Icons.Outlined.BarChart),
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VitaCalTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

  DisposableEffect(auth) {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      currentUser = firebaseAuth.currentUser
    }
    auth.addAuthStateListener(listener)
    onDispose {
      auth.removeAuthStateListener(listener)
    }
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(
      onAuthSuccess = { currentUser = auth.currentUser },
    )
  } else {
    key(user.uid) {
      MainFeatureScreen(
        currentUserId = user.uid,
        displayName = user.displayName ?: "Atleta VitaCal",
        userEmail = user.email,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeatureScreen(
  currentUserId: String,
  displayName: String,
  userEmail: String?,
) {
  val viewModel: NutritionViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        NutritionViewModel(
          repository = NutritionRepository(db, Firebase.auth),
          currentUserId = currentUserId,
          initialDisplayName = displayName,
        )
      }
    },
  )

  val selectedDateKey by viewModel.selectedDateKey.collectAsStateWithLifecycle()
  val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
  val entriesState by viewModel.dailyEntriesState.collectAsStateWithLifecycle()
  val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
  val waterMl by viewModel.waterMlState.collectAsStateWithLifecycle()
  val catalogFoods by viewModel.allCatalogFoods.collectAsStateWithLifecycle()
  val weeklyPoints by viewModel.weeklyCaloriesPoints.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DIARY

  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var targetMealType by remember { mutableStateOf(MealType.LUNCH) }
  var foodToLog by remember { mutableStateOf<CatalogFood?>(null) }
  var entryToEdit by remember { mutableStateOf<MealEntry?>(null) }
  var showQuickCalorieDialog by remember { mutableStateOf(false) }
  var showCreateCustomFoodDialog by remember { mutableStateOf(false) }

  LaunchedEffect(snackbarMessage) {
    val msg = snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail) {
        NavigationRail(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxHeight(),
        ) {
          NAV_TABS.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTab(tab.route) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = stringResource(tab.labelRes),
                )
              },
              label = { Text(stringResource(tab.labelRes)) },
              modifier = Modifier.testTag("nav_rail_${tab.route}"),
            )
          }
        }
      }

      Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          TopAppBar(
            title = {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.background,
            ),
          )
        },
        bottomBar = {
          if (!useNavigationRail) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              windowInsets = WindowInsets.navigationBars,
            ) {
              NAV_TABS.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateToTab(tab.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                      contentDescription = stringResource(tab.labelRes),
                    )
                  },
                  label = { Text(stringResource(tab.labelRes)) },
                  modifier = Modifier.testTag("nav_tab_${tab.route}"),
                )
              }
            }
          }
        },
        floatingActionButton = {
          if (currentRoute == ROUTE_DIARY) {
            ExtendedFloatingActionButton(
              onClick = {
                targetMealType = MealType.LUNCH
                navigateToTab(ROUTE_FOODS)
              },
              icon = { Icon(Icons.Filled.Add, contentDescription = null) },
              text = { Text(stringResource(R.string.add_food_fab)) },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.testTag("fab_add_food"),
            )
          }
        },
        modifier = Modifier.weight(1f),
      ) { innerPadding ->
        NavHost(
          navController = navController,
          startDestination = ROUTE_DIARY,
          modifier = Modifier.padding(innerPadding),
        ) {
          composable(ROUTE_DIARY) {
            DiaryScreen(
              selectedDateKey = selectedDateKey,
              profileState = profileState,
              entriesState = entriesState,
              summary = dailySummary,
              waterMl = waterMl,
              onPreviousDay = { viewModel.shiftSelectedDate(-1) },
              onNextDay = { viewModel.shiftSelectedDate(1) },
              onGoToToday = { viewModel.selectToday() },
              onAdjustWater = { viewModel.adjustWater(it) },
              onAddFoodForMeal = { mealType ->
                targetMealType = mealType
                navigateToTab(ROUTE_FOODS)
              },
              onQuickEntryClick = { mealType ->
                targetMealType = mealType
                showQuickCalorieDialog = true
              },
              onEntryClick = { entry -> entryToEdit = entry },
            )
          }

          composable(ROUTE_FOODS) {
            FoodsScreen(
              foods = catalogFoods,
              targetMealType = targetMealType,
              onSelectTargetMeal = { targetMealType = it },
              onFoodClick = { food -> foodToLog = food },
              onQuickEntryClick = { showQuickCalorieDialog = true },
              onCreateCustomFoodClick = { showCreateCustomFoodDialog = true },
              onDeleteCustomFood = { id -> viewModel.deleteCustomFood(id) },
            )
          }

          composable(ROUTE_PROGRESS) {
            ProgressScreen(
              userEmail = userEmail,
              profileState = profileState,
              weeklyPoints = weeklyPoints,
              onSaveProfile = { updated -> viewModel.saveUserProfile(updated) },
              onSignOut = {
                signOut(
                  credentialManager = credentialManager,
                  scope = scope,
                )
              },
            )
          }
        }
      }
    }
  }

  foodToLog?.let { selectedFood ->
    AddFoodToMealDialog(
      food = selectedFood,
      initialMealType = targetMealType,
      onDismiss = { foodToLog = null },
      onConfirm = { mealType, servings ->
        viewModel.addFoodToMeal(selectedFood, mealType, servings)
        foodToLog = null
        navigateToTab(ROUTE_DIARY)
      },
    )
  }

  entryToEdit?.let { entry ->
    EditMealEntryDialog(
      entry = entry,
      onDismiss = { entryToEdit = null },
      onSave = { newServings, newMealType ->
        viewModel.updateMealEntryServings(entry, newServings, newMealType)
        entryToEdit = null
      },
      onDelete = {
        viewModel.deleteMealEntry(entry.id)
        entryToEdit = null
      },
    )
  }

  if (showQuickCalorieDialog) {
    QuickCalorieDialog(
      initialMealType = targetMealType,
      onDismiss = { showQuickCalorieDialog = false },
      onConfirm = { name, mealType, kcal, p, c, f ->
        viewModel.addQuickEntry(name, mealType, kcal, p, c, f)
        showQuickCalorieDialog = false
        navigateToTab(ROUTE_DIARY)
      },
    )
  }

  if (showCreateCustomFoodDialog) {
    CreateCustomFoodDialog(
      onDismiss = { showCreateCustomFoodDialog = false },
      onCreate = { name, serving, kcal, p, c, f, meal ->
        viewModel.createCustomFood(name, serving, kcal, p, c, f, meal)
        showCreateCustomFoodDialog = false
      },
    )
  }
}
