package com.example.apiapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apiapp.ui.theme.ReadoutFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: CharacterViewModel) {
    val allCharacters by viewModel.allCharacters.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val cachedCount = allCharacters.size
    val favoritesCount = allCharacters.count { it.isFavorite }
    val statusCounts = allCharacters.groupingBy { it.status }.eachCount().entries.sortedByDescending { it.value }
    val speciesCounts = allCharacters.groupingBy { it.species }.eachCount().entries
        .sortedByDescending { it.value }
        .take(5)
    val paletteColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
            ) {
                TopAppBar(
                    title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Tu colección", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            val total = totalCount
            if (total != null && total > 0) {
                StatBar(
                    label = "Personajes explorados",
                    count = cachedCount,
                    max = total,
                    color = MaterialTheme.colorScheme.primary,
                    trailingText = "$cachedCount / $total"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Favoritos",
                    value = favoritesCount.toString(),
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "En cache",
                    value = cachedCount.toString(),
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Por estado", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            statusCounts.forEach { (status, count) ->
                StatBar(label = status, count = count, max = cachedCount, color = statusColor(status))
            }

            if (speciesCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Especies más comunes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                speciesCounts.forEachIndexed { index, (species, count) ->
                    StatBar(
                        label = species,
                        count = count,
                        max = cachedCount,
                        color = paletteColors[index % paletteColors.size]
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                fontFamily = ReadoutFontFamily,
                color = accent
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatBar(label: String, count: Int, max: Int, color: Color, trailingText: String = count.toString()) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(trailingText, style = MaterialTheme.typography.bodyMedium, fontFamily = ReadoutFontFamily, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = if (max > 0) (count.toFloat() / max).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(color)
            )
        }
    }
}
