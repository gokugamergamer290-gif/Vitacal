package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BrazilianFoodCatalog
import com.example.data.model.CatalogFood
import com.example.data.model.MealType
import com.example.ui.theme.CarbsOchre
import com.example.ui.theme.FatTeal
import com.example.ui.theme.ProteinCoral
import kotlin.math.roundToInt

@Composable
fun FoodsScreen(
  foods: List<CatalogFood>,
  targetMealType: MealType,
  onSelectTargetMeal: (MealType) -> Unit,
  onFoodClick: (CatalogFood) -> Unit,
  onQuickEntryClick: () -> Unit,
  onCreateCustomFoodClick: () -> Unit,
  onDeleteCustomFood: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Todos") }

  val filteredFoods = remember(foods, searchQuery, selectedCategory) {
    val q = searchQuery.trim().lowercase()
    foods.filter { food ->
      val matchesCategory = when (selectedCategory) {
        "Todos" -> true
        "Meus Alimentos" -> food.isCustom
        else -> food.category.equals(selectedCategory, ignoreCase = true)
      }
      val matchesQuery = q.isEmpty() ||
        food.name.lowercase().contains(q) ||
        food.category.lowercase().contains(q)
      matchesCategory && matchesQuery
    }
  }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .widthIn(max = 680.dp)
        .fillMaxSize(),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Catálogo de Alimentos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "Adicionando para: ${targetMealType.labelPt}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
          )

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(MealType.entries) { meal ->
              FilterChip(
                selected = targetMealType == meal,
                onClick = { onSelectTargetMeal(meal) },
                label = { Text(meal.labelPt) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
              )
            }
          }

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar alimento (ex: arroz, frango, ovo)") },
            leadingIcon = {
              Icon(Icons.Filled.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Filled.Clear, contentDescription = "Limpar busca")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("food_search_input"),
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            FilledTonalButton(
              onClick = onQuickEntryClick,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("foods_quick_entry_button"),
            ) {
              Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Entrada rápida", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
              onClick = onCreateCustomFoodClick,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("create_custom_food_button"),
            ) {
              Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Criar alimento", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(BrazilianFoodCatalog.categories) { category ->
              FilterChip(
                selected = selectedCategory == category,
                onClick = { selectedCategory = category },
                label = { Text(category) },
                modifier = Modifier.testTag("category_chip_$category"),
              )
            }
          }
        }
      }

      if (filteredFoods.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Nenhum alimento encontrado",
                style = MaterialTheme.typography.titleMedium,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Crie um alimento personalizado ou use a entrada rápida de calorias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        items(filteredFoods, key = { "${it.isCustom}_${it.id}" }) { food ->
          FoodCatalogCard(
            food = food,
            onClick = { onFoodClick(food) },
            onDeleteCustom = if (food.isCustom) {
              { onDeleteCustomFood(food.id) }
            } else {
              null
            },
          )
        }
      }
    }
  }
}

@Composable
private fun FoodCatalogCard(
  food: CatalogFood,
  onClick: () -> Unit,
  onDeleteCustom: (() -> Unit)?,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("catalog_food_${food.id}"),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = food.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          if (food.isCustom) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "Meu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${food.servingLabel}  •  ${food.category}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          MacroChip("P ${food.proteinG.roundToInt()}g", ProteinCoral)
          MacroChip("C ${food.carbsG.roundToInt()}g", CarbsOchre)
          MacroChip("G ${food.fatG.roundToInt()}g", FatTeal)
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDeleteCustom != null) {
          IconButton(
            onClick = onDeleteCustom,
            modifier = Modifier.size(48.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Excluir ${food.name}",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Text(
              text = "${food.calories}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "kcal",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MacroChip(
  label: String,
  color: androidx.compose.ui.graphics.Color,
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(color.copy(alpha = 0.12f))
      .padding(horizontal = 6.dp, vertical = 2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = color,
    )
  }
}
