package com.example.apiapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.data.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(first: Character, second: Character, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
            ) {
                TopAppBar(
                    title = { Text("Comparar personajes", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
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
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CompareHeader(first, modifier = Modifier.weight(1f))
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                CompareHeader(second, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            val commonEpisodes = first.episodeIds.toSet().intersect(second.episodeIds.toSet()).size

            CompareRow(label = "Estado", valueA = first.status, valueB = second.status)
            CompareRow(label = "Especie", valueA = first.species, valueB = second.species)
            CompareRow(label = "Género", valueA = first.gender, valueB = second.gender)
            CompareRow(label = "Origen", valueA = first.originName, valueB = second.originName)
            CompareRow(label = "Última ubicación", valueA = first.locationName, valueB = second.locationName)
            CompareRow(
                label = "Episodios",
                valueA = "${first.episodeCount} apariciones",
                valueB = "${second.episodeCount} apariciones"
            )
            CompareRow(
                label = "Episodios en común",
                valueA = "$commonEpisodes",
                valueB = "$commonEpisodes"
            )
        }
    }
}

@Composable
private fun CompareHeader(character: Character, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = character.imageUrl,
            contentDescription = character.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(MaterialTheme.shapes.large)
                .border(3.dp, statusColor(character.status), MaterialTheme.shapes.large)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = character.name,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompareRow(label: String, valueA: String, valueB: String) {
    val isMatch = valueA.equals(valueB, ignoreCase = true) && valueA.isNotBlank()
    val matchColor = if (isMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = valueA.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal,
                color = matchColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueB.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal,
                color = matchColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
